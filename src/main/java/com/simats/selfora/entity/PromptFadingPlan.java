package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_fading_plans")
public class PromptFadingPlan {
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
    @JoinColumn(name = "step_id", nullable = false)
    private TaskStep step;

    @ManyToOne
    @JoinColumn(name = "current_prompt_level_id", nullable = false)
    private PromptLevel currentPromptLevel;

    @ManyToOne
    @JoinColumn(name = "target_prompt_level_id", nullable = false)
    private PromptLevel targetPromptLevel;

    @Column(name = "clinical_reason", columnDefinition = "TEXT")
    private String clinicalReason;

    @Column(name = "review_criteria", columnDefinition = "TEXT")
    private String reviewCriteria;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 30)
    private String status; // SUGGESTED, UNDER_REVIEW, APPROVED, ACTIVE, REJECTED, CANCELLED

    @ManyToOne
    @JoinColumn(name = "approved_by_therapist_id")
    private User approvedByTherapist;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public PromptFadingPlan() {}

    public PromptFadingPlan(Long id, Child child, User therapist, Activity activity, TaskStep step, PromptLevel currentPromptLevel, PromptLevel targetPromptLevel, String clinicalReason, String reviewCriteria, String notes, String status, User approvedByTherapist, LocalDateTime approvedAt, LocalDateTime createdAt) {
        this.id = id;
        this.child = child;
        this.therapist = therapist;
        this.activity = activity;
        this.step = step;
        this.currentPromptLevel = currentPromptLevel;
        this.targetPromptLevel = targetPromptLevel;
        this.clinicalReason = clinicalReason;
        this.reviewCriteria = reviewCriteria;
        this.notes = notes;
        this.status = status;
        this.approvedByTherapist = approvedByTherapist;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private User therapist;
        private Activity activity;
        private TaskStep step;
        private PromptLevel currentPromptLevel;
        private PromptLevel targetPromptLevel;
        private String clinicalReason;
        private String reviewCriteria;
        private String notes;
        private String status = "SUGGESTED";
        private User approvedByTherapist;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder therapist(User therapist) { this.therapist = therapist; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder currentPromptLevel(PromptLevel currentPromptLevel) { this.currentPromptLevel = currentPromptLevel; return this; }
        public Builder targetPromptLevel(PromptLevel targetPromptLevel) { this.targetPromptLevel = targetPromptLevel; return this; }
        public Builder clinicalReason(String clinicalReason) { this.clinicalReason = clinicalReason; return this; }
        public Builder reviewCriteria(String reviewCriteria) { this.reviewCriteria = reviewCriteria; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder approvedByTherapist(User approvedByTherapist) { this.approvedByTherapist = approvedByTherapist; return this; }
        public Builder approvedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PromptFadingPlan build() { return new PromptFadingPlan(id, child, therapist, activity, step, currentPromptLevel, targetPromptLevel, clinicalReason, reviewCriteria, notes, status, approvedByTherapist, approvedAt, createdAt); }
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

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public PromptLevel getCurrentPromptLevel() { return currentPromptLevel; }
    public void setCurrentPromptLevel(PromptLevel currentPromptLevel) { this.currentPromptLevel = currentPromptLevel; }

    public PromptLevel getTargetPromptLevel() { return targetPromptLevel; }
    public void setTargetPromptLevel(PromptLevel targetPromptLevel) { this.targetPromptLevel = targetPromptLevel; }

    public String getClinicalReason() { return clinicalReason; }
    public void setClinicalReason(String clinicalReason) { this.clinicalReason = clinicalReason; }

    public String getReviewCriteria() { return reviewCriteria; }
    public void setReviewCriteria(String reviewCriteria) { this.reviewCriteria = reviewCriteria; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getApprovedByTherapist() { return approvedByTherapist; }
    public void setApprovedByTherapist(User approvedByTherapist) { this.approvedByTherapist = approvedByTherapist; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
