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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessagingService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    public MessageDtos.MessageResponse sendMessage(MessageDtos.SendMessageRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .child(child)
                .messageText(request.getMessageText())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        return mapToResponse(saved);
    }

    public List<MessageDtos.MessageResponse> getConversationForChild(Long childId) {
        List<Message> messages = messageRepository.findConversationByChildId(childId);
        return messages.stream().map(this::mapToResponse).collect(Collectors.toList());
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
