package com.simats.selfora.dto;

import java.time.LocalDateTime;

public class MessageDtos {

    public static class SendMessageRequest {
        private Long receiverId;
        private Long childId;
        private String messageText;

        public SendMessageRequest() {}

        public SendMessageRequest(Long receiverId, Long childId, String messageText) {
            this.receiverId = receiverId;
            this.childId = childId;
            this.messageText = messageText;
        }

        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getMessageText() { return messageText; }
        public void setMessageText(String messageText) { this.messageText = messageText; }
    }

    public static class MessageResponse {
        private Long id;
        private Long senderId;
        private String senderName;
        private Long receiverId;
        private String receiverName;
        private Long childId;
        private String messageText;
        private Boolean isRead;
        private LocalDateTime sentAt;

        public MessageResponse() {}

        public MessageResponse(Long id, Long senderId, String senderName, Long receiverId, String receiverName, Long childId, String messageText, Boolean isRead, LocalDateTime sentAt) {
            this.id = id;
            this.senderId = senderId;
            this.senderName = senderName;
            this.receiverId = receiverId;
            this.receiverName = receiverName;
            this.childId = childId;
            this.messageText = messageText;
            this.isRead = isRead != null ? isRead : false;
            this.sentAt = sentAt;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long senderId;
            private String senderName;
            private Long receiverId;
            private String receiverName;
            private Long childId;
            private String messageText;
            private Boolean isRead = false;
            private LocalDateTime sentAt;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder senderId(Long senderId) { this.senderId = senderId; return this; }
            public Builder senderName(String senderName) { this.senderName = senderName; return this; }
            public Builder receiverId(Long receiverId) { this.receiverId = receiverId; return this; }
            public Builder receiverName(String receiverName) { this.receiverName = receiverName; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder messageText(String messageText) { this.messageText = messageText; return this; }
            public Builder isRead(Boolean isRead) { this.isRead = isRead; return this; }
            public Builder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }
            public MessageResponse build() { return new MessageResponse(id, senderId, senderName, receiverId, receiverName, childId, messageText, isRead, sentAt); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        public String getReceiverName() { return receiverName; }
        public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getMessageText() { return messageText; }
        public void setMessageText(String messageText) { this.messageText = messageText; }
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    }
}
