package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    public Message() {}

    public Message(Long id, User sender, User receiver, Child child, String messageText, Boolean isRead, LocalDateTime sentAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.child = child;
        this.messageText = messageText;
        this.isRead = isRead != null ? isRead : false;
        this.sentAt = sentAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private User sender;
        private User receiver;
        private Child child;
        private String messageText;
        private Boolean isRead = false;
        private LocalDateTime sentAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder sender(User sender) { this.sender = sender; return this; }
        public Builder receiver(User receiver) { this.receiver = receiver; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder messageText(String messageText) { this.messageText = messageText; return this; }
        public Builder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public Builder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }
        public Message build() { return new Message(id, sender, receiver, child, messageText, isRead, sentAt); }
    }

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
