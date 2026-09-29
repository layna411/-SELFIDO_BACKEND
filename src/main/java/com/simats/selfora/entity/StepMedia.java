package com.simats.selfora.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "step_media")
public class StepMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private TaskStep step;

    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(length = 255)
    private String caption;

    public StepMedia() {}

    public StepMedia(Long id, TaskStep step, String mediaType, String mediaUrl, String thumbnailUrl, String caption) {
        this.id = id;
        this.step = step;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.caption = caption;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private TaskStep step;
        private String mediaType;
        private String mediaUrl;
        private String thumbnailUrl;
        private String caption;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder mediaType(String mediaType) { this.mediaType = mediaType; return this; }
        public Builder mediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; return this; }
        public Builder thumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; return this; }
        public Builder caption(String caption) { this.caption = caption; return this; }
        public StepMedia build() { return new StepMedia(id, step, mediaType, mediaUrl, thumbnailUrl, caption); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
