package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "child_mode_preferences")
public class ChildModePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "child_id", nullable = false, unique = true)
    private Child child;

    @Column(name = "preferred_name", length = 100)
    private String preferredName;

    @Column(name = "audio_enabled")
    private Boolean audioEnabled = true;

    @Column(name = "animation_enabled")
    private Boolean animationEnabled = true;

    @Column(name = "reduced_motion")
    private Boolean reducedMotion = false;

    @Column(length = 20)
    private String language = "EN";

    @Column(name = "reward_sounds")
    private Boolean rewardSounds = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ChildModePreference() {}

    public ChildModePreference(Child child, String preferredName) {
        this.child = child;
        this.preferredName = preferredName;
        this.audioEnabled = true;
        this.animationEnabled = true;
        this.reducedMotion = false;
        this.language = "EN";
        this.rewardSounds = true;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public String getPreferredName() { return preferredName; }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }

    public Boolean getAudioEnabled() { return audioEnabled; }
    public void setAudioEnabled(Boolean audioEnabled) { this.audioEnabled = audioEnabled; }

    public Boolean getAnimationEnabled() { return animationEnabled; }
    public void setAnimationEnabled(Boolean animationEnabled) { this.animationEnabled = animationEnabled; }

    public Boolean getReducedMotion() { return reducedMotion; }
    public void setReducedMotion(Boolean reducedMotion) { this.reducedMotion = reducedMotion; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getRewardSounds() { return rewardSounds; }
    public void setRewardSounds(Boolean rewardSounds) { this.rewardSounds = rewardSounds; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
