package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adaptive_plans")
public class AdaptivePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @JoinColumn(name = "therapist_id", nullable = false)
    private User therapist;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TherapySession session;

    @ManyToOne
    @JoinColumn(name = "current_prompt_level_id", nullable = false)
    private PromptLevel currentPromptLevel;

    @ManyToOne
    @JoinColumn(name = "target_prompt_level_id", nullable = false)
    private PromptLevel targetPromptLevel;

    @Column(name = "target_step_ids", nullable = false, columnDefinition = "JSON")
    private String targetStepIds;

    @Column(name = "clinical_rationale", columnDefinition = "TEXT")
    private String clinicalRationale;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AdaptivePlan() {}

    public AdaptivePlan(Long id, Child child, User therapist, Activity activity, TherapySession session, PromptLevel currentPromptLevel, PromptLevel targetPromptLevel, String targetStepIds, String clinicalRationale, Boolean isConfirmed, LocalDateTime createdAt) {
        this.id = id;
        this.child = child;
        this.therapist = therapist;
        this.activity = activity;
        this.session = session;
        this.currentPromptLevel = currentPromptLevel;
        this.targetPromptLevel = targetPromptLevel;
        this.targetStepIds = targetStepIds;
        this.clinicalRationale = clinicalRationale;
        this.isConfirmed = isConfirmed != null ? isConfirmed : false;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private User therapist;
        private Activity activity;
        private TherapySession session;
        private PromptLevel currentPromptLevel;
        private PromptLevel targetPromptLevel;
        private String targetStepIds;
        private String clinicalRationale;
        private Boolean isConfirmed = false;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder therapist(User therapist) { this.therapist = therapist; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder session(TherapySession session) { this.session = session; return this; }
        public Builder currentPromptLevel(PromptLevel currentPromptLevel) { this.currentPromptLevel = currentPromptLevel; return this; }
        public Builder targetPromptLevel(PromptLevel targetPromptLevel) { this.targetPromptLevel = targetPromptLevel; return this; }
        public Builder targetStepIds(String targetStepIds) { this.targetStepIds = targetStepIds; return this; }
        public Builder clinicalRationale(String clinicalRationale) { this.clinicalRationale = clinicalRationale; return this; }
        public Builder isConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AdaptivePlan build() { return new AdaptivePlan(id, child, therapist, activity, session, currentPromptLevel, targetPromptLevel, targetStepIds, clinicalRationale, isConfirmed, createdAt); }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public User getTherapist() { return therapist; }
    public void setTherapist(User therapist) { this.therapist = therapist; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public TherapySession getSession() { return session; }
    public void setSession(TherapySession session) { this.session = session; }

    public PromptLevel getCurrentPromptLevel() { return currentPromptLevel; }
    public void setCurrentPromptLevel(PromptLevel currentPromptLevel) { this.currentPromptLevel = currentPromptLevel; }

    public PromptLevel getTargetPromptLevel() { return targetPromptLevel; }
    public void setTargetPromptLevel(PromptLevel targetPromptLevel) { this.targetPromptLevel = targetPromptLevel; }

    public String getTargetStepIds() { return targetStepIds; }
    public void setTargetStepIds(String targetStepIds) { this.targetStepIds = targetStepIds; }

    public String getClinicalRationale() { return clinicalRationale; }
    public void setClinicalRationale(String clinicalRationale) { this.clinicalRationale = clinicalRationale; }

    public Boolean getIsConfirmed() { return isConfirmed; }
    public void setIsConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
