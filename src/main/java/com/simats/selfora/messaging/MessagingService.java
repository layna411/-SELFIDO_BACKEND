package com.simats.selfora.messaging;

import com.simats.selfora.dto.MessageDtos;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.Message;
import com.simats.selfora.entity.User;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.ChildRepository;
import com.simats.selfora.repository.MessageRepository;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessagingService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private com.simats.selfora.child.ChildService childService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ChatSessionRegistry chatSessionRegistry;

    @Transactional
    public MessageDtos.MessageResponse sendMessage(MessageDtos.SendMessageRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found"));
        return sendMessageAsUser(sender, request);
    }

    @Transactional
    public MessageDtos.MessageResponse sendMessageAsUser(User sender, MessageDtos.SendMessageRequest request) {
        if (sender == null) {
            throw new AccessDeniedException("Sender not authenticated");
        }
        if (request == null || request.getChildId() == null || request.getMessageText() == null || request.getMessageText().trim().isEmpty()) {
            throw new IllegalArgumentException("childId and non-empty messageText are required");
        }

        Long childId = request.getChildId();
        validateAccessForUser(sender, childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        User receiver = resolveReceiverForChild(sender, request.getReceiverId(), childId);

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .child(child)
                .messageText(request.getMessageText().trim())
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);
        MessageDtos.MessageResponse response = mapToResponse(saved);

        // Broadcast instantly over WebSocket to all linked participants for this child
        Set<Long> participants = getAllParticipantUserIdsForChild(childId);
        participants.add(sender.getId());
        participants.add(receiver.getId());
        chatSessionRegistry.broadcastNewMessage(response, participants);

        return response;
    }

    @Transactional
    public List<MessageDtos.MessageResponse> getConversationForChild(Long childId) {
        if (childId != null) {
            childService.validateChildAccessForCurrentUser(childId);
        }
        List<Message> messages = messageRepository.findConversationByChildId(childId);
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null) {
                markUnreadMessagesInternal(currentUser, childId, messages);
            }
        } catch (Exception ignored) {}

        return messages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void markConversationAsRead(Long childId) {
        if (childId != null) {
            childService.validateChildAccessForCurrentUser(childId);
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser != null) {
            markConversationAsReadAsUser(currentUser, childId);
        }
    }

    @Transactional
    public void markConversationAsReadAsUser(User currentUser, Long childId) {
        if (currentUser == null || childId == null) return;
        validateAccessForUser(currentUser, childId);
        List<Message> messages = messageRepository.findConversationByChildId(childId);
        markUnreadMessagesInternal(currentUser, childId, messages);
    }

    private void markUnreadMessagesInternal(User currentUser, Long childId, List<Message> messages) {
        boolean updated = false;
        for (Message m : messages) {
            if (m.getReceiver() != null && currentUser.getId().equals(m.getReceiver().getId()) && !Boolean.TRUE.equals(m.getIsRead())) {
                m.setIsRead(true);
                updated = true;
            }
        }
        if (updated) {
            messageRepository.saveAll(messages);
            Set<Long> participants = getAllParticipantUserIdsForChild(childId);
            chatSessionRegistry.broadcastMessagesRead(childId, currentUser.getId(), participants);
        }
    }

    public MessageDtos.ChatPartnerResponse getChatPartnerForChild(Long childId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateAccessForUser(currentUser, childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
        String childName = child.getFirstName() + (child.getLastName() != null && !child.getLastName().isBlank() ? " " + child.getLastName() : "");

        boolean isCaregiver = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_CAREGIVER".equalsIgnoreCase(r.getName()));

        if (isCaregiver) {
            User therapist = findAssignedTherapistForChild(childId);
            if (therapist != null) {
                return new MessageDtos.ChatPartnerResponse(therapist.getId(), therapist.getFullName(), "Occupational Therapist", childId, childName);
            }
        } else {
            User caregiver = findLinkedCaregiverForChild(childId);
            if (caregiver != null) {
                return new MessageDtos.ChatPartnerResponse(caregiver.getId(), caregiver.getFullName(), "Caregiver", childId, childName);
            }
        }
        throw new ResourceNotFoundException("Chat partner not assigned for child ID: " + childId);
    }

    private void validateAccessForUser(User user, Long childId) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()) || "ROLE_SUPER_ADMIN".equalsIgnoreCase(r.getName()));
        if (isAdmin) return;

        boolean isTherapist = user.getRoles().stream().anyMatch(r -> "ROLE_THERAPIST".equalsIgnoreCase(r.getName()));
        if (isTherapist && childRepository.countByChildIdAndTherapistId(childId, user.getId()) > 0) return;

        boolean isCaregiver = user.getRoles().stream().anyMatch(r -> "ROLE_CAREGIVER".equalsIgnoreCase(r.getName()));
        if (isCaregiver && childRepository.countByChildIdAndCaregiverId(childId, user.getId()) > 0) return;

        throw new AccessDeniedException("User " + user.getUsername() + " does not have access to child ID: " + childId);
    }

    private User resolveReceiverForChild(User sender, Long requestedReceiverId, Long childId) {
        boolean isCaregiverSender = sender.getRoles().stream().anyMatch(r -> "ROLE_CAREGIVER".equalsIgnoreCase(r.getName()));
        boolean isTherapistSender = sender.getRoles().stream().anyMatch(r -> "ROLE_THERAPIST".equalsIgnoreCase(r.getName()));

        // If sender is Caregiver, ALWAYS route to a valid Therapist assigned to this child unless a valid different therapist ID was passed
        if (isCaregiverSender && !isTherapistSender) {
            if (requestedReceiverId != null && requestedReceiverId > 0 && !requestedReceiverId.equals(sender.getId())) {
                Optional<User> reqOpt = userRepository.findById(requestedReceiverId);
                if (reqOpt.isPresent()) {
                    boolean reqIsTherapist = reqOpt.get().getRoles().stream()
                            .anyMatch(r -> "ROLE_THERAPIST".equalsIgnoreCase(r.getName()) || "ROLE_SUPER_ADMIN".equalsIgnoreCase(r.getName()));
                    if (reqIsTherapist) {
                        return reqOpt.get();
                    }
                }
            }
            User therapist = findAssignedTherapistForChild(childId);
            if (therapist != null) return therapist;
        }

        // If sender is Therapist, resolve caregiver for this child if requestedReceiverId is invalid or self
        if (isTherapistSender) {
            if (requestedReceiverId != null && requestedReceiverId > 0 && !requestedReceiverId.equals(sender.getId())) {
                Optional<User> reqOpt = userRepository.findById(requestedReceiverId);
                if (reqOpt.isPresent()) return reqOpt.get();
            }
            User caregiver = findLinkedCaregiverForChild(childId);
            if (caregiver != null) return caregiver;
        }

        if (requestedReceiverId != null && requestedReceiverId > 0) {
            return userRepository.findById(requestedReceiverId)
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found"));
        }
        throw new ResourceNotFoundException("Unable to determine valid receiver for child ID: " + childId);
    }

    private User findAssignedTherapistForChild(Long childId) {
        try {
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT therapist_id FROM therapist_children WHERE child_id = ? LIMIT 1",
                    Long.class, childId
            );
            if (!ids.isEmpty()) {
                return userRepository.findById(ids.get(0)).orElse(null);
            }
            // Fallback to first therapist in system if not explicitly mapped
            List<Long> fallbackIds = jdbcTemplate.queryForList(
                    "SELECT u.id FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ROLE_THERAPIST' LIMIT 1",
                    Long.class
            );
            if (!fallbackIds.isEmpty()) {
                return userRepository.findById(fallbackIds.get(0)).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private User findLinkedCaregiverForChild(Long childId) {
        try {
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT caregiver_id FROM caregiver_children WHERE child_id = ? LIMIT 1",
                    Long.class, childId
            );
            if (!ids.isEmpty()) {
                return userRepository.findById(ids.get(0)).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Set<Long> getAllParticipantUserIdsForChild(Long childId) {
        Set<Long> ids = new HashSet<>();
        try {
            ids.addAll(jdbcTemplate.queryForList("SELECT therapist_id FROM therapist_children WHERE child_id = ?", Long.class, childId));
            ids.addAll(jdbcTemplate.queryForList("SELECT caregiver_id FROM caregiver_children WHERE child_id = ?", Long.class, childId));
        } catch (Exception ignored) {}
        return ids;
    }

    private MessageDtos.MessageResponse mapToResponse(Message m) {
        return MessageDtos.MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getFullName())
                .childId(m.getChild().getId())
                .messageText(m.getMessageText())
                .isRead(m.getIsRead())
                .sentAt(m.getSentAt())
                .build();
    }
}
