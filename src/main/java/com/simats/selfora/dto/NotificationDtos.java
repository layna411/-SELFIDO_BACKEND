package com.simats.selfora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NotificationDtos {

    public static class NotificationItemResponse {
        private Long id;
        private Long userId;
        private String title;
        private String message;
        private String notificationType;
        private Long referenceId;

        @JsonProperty("isRead")
        private Boolean isRead;

        private LocalDateTime createdAt;

        public NotificationItemResponse() {}

        public NotificationItemResponse(Long id, Long userId, String title, String message, String notificationType, Long referenceId, Boolean isRead, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.notificationType = notificationType;
            this.referenceId = referenceId;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getNotificationType() { return notificationType; }
        public Long getReferenceId() { return referenceId; }

        @JsonProperty("isRead")
        public Boolean getIsRead() { return isRead; }

        @JsonProperty("read")
        public Boolean getRead() { return isRead; }

        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
