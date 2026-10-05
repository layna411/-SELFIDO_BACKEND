package com.simats.selfora.homeprogram;

import com.simats.selfora.dto.NotificationDtos;
import com.simats.selfora.entity.ProgrammeNotification;
import com.simats.selfora.entity.User;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.ProgrammeNotificationRepository;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private ProgrammeNotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void sendNotification(User targetUser, String title, String message, String type, Long referenceId) {
        if (targetUser == null) return;
        ProgrammeNotification notification = ProgrammeNotification.builder()
                .user(targetUser)
                .title(title)
                .message(message)
                .notificationType(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationDtos.NotificationItemResponse> getNotificationsForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        ProgrammeNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    private NotificationDtos.NotificationItemResponse mapToResponse(ProgrammeNotification n) {
        return new NotificationDtos.NotificationItemResponse(
                n.getId(), n.getUser().getId(), n.getTitle(), n.getMessage(), n.getNotificationType(), n.getReferenceId(), n.getIsRead(), n.getCreatedAt()
        );
    }
}
