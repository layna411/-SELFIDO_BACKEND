package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ActivityCategory category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "title_ta", length = 150)
    private String titleTa;

    @Column(name = "target_gender", length = 20)
    private String targetGender;

    @Column(name = "starting_position", length = 500)
    private String startingPosition;

    @Column(name = "starting_position_ta", columnDefinition = "TEXT")
    private String startingPositionTa;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_ta", columnDefinition = "TEXT")
    private String descriptionTa;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Activity() {}

    public Activity(Long id, ActivityCategory category, String title, String titleTa, String targetGender, String startingPosition, String startingPositionTa, String description, String descriptionTa, String iconUrl, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.titleTa = titleTa;
        this.targetGender = targetGender;
        this.startingPosition = startingPosition;
        this.startingPositionTa = startingPositionTa;
        this.description = description;
        this.descriptionTa = descriptionTa;
        this.iconUrl = iconUrl;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private ActivityCategory category;
        private String title;
        private String titleTa;
        private String targetGender;
        private String startingPosition;
        private String startingPositionTa;
        private String description;
        private String descriptionTa;
        private String iconUrl;
        private Boolean isActive = true;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder category(ActivityCategory category) { this.category = category; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder titleTa(String titleTa) { this.titleTa = titleTa; return this; }
        public Builder targetGender(String targetGender) { this.targetGender = targetGender; return this; }
        public Builder startingPosition(String startingPosition) { this.startingPosition = startingPosition; return this; }
        public Builder startingPositionTa(String startingPositionTa) { this.startingPositionTa = startingPositionTa; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder descriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; return this; }
        public Builder iconUrl(String iconUrl) { this.iconUrl = iconUrl; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Activity build() { return new Activity(id, category, title, titleTa, targetGender, startingPosition, startingPositionTa, description, descriptionTa, iconUrl, isActive, createdAt); }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ActivityCategory getCategory() { return category; }
    public void setCategory(ActivityCategory category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getTargetGender() { return targetGender; }
    public void setTargetGender(String targetGender) { this.targetGender = targetGender; }

    public String getStartingPosition() { return startingPosition; }
    public void setStartingPosition(String startingPosition) { this.startingPosition = startingPosition; }

    public String getStartingPositionTa() { return startingPositionTa; }
    public void setStartingPositionTa(String startingPositionTa) { this.startingPositionTa = startingPositionTa; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
