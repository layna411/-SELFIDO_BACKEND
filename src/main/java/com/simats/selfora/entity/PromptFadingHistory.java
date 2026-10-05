package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_fading_histories")
public class PromptFadingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private TaskStep step;

    @ManyToOne
    @JoinColumn(name = "approved_by_therapist_id", nullable = false)
    private User approvedByTherapist;

    @ManyToOne
    @JoinColumn(name = "previous_prompt_level_id", nullable = false)
    private PromptLevel previousPromptLevel;

    @ManyToOne
    @JoinColumn(name = "new_prompt_level_id", nullable = false)
    private PromptLevel newPromptLevel;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "subsequent_performance_notes", columnDefinition = "TEXT")
    private String subsequentPerformanceNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public PromptFadingHistory() {}

    public PromptFadingHistory(Long id, Child child, TaskStep step, User approvedByTherapist, PromptLevel previousPromptLevel, PromptLevel newPromptLevel, LocalDateTime changeDate, String reason, String subsequentPerformanceNotes, LocalDateTime createdAt) {
        this.id = id;
        this.child = child;
        this.step = step;
        this.approvedByTherapist = approvedByTherapist;
        this.previousPromptLevel = previousPromptLevel;
        this.newPromptLevel = newPromptLevel;
        this.changeDate = changeDate;
        this.reason = reason;
        this.subsequentPerformanceNotes = subsequentPerformanceNotes;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private TaskStep step;
        private User approvedByTherapist;
        private PromptLevel previousPromptLevel;
        private PromptLevel newPromptLevel;
        private LocalDateTime changeDate;
        private String reason;
        private String subsequentPerformanceNotes;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder approvedByTherapist(User approvedByTherapist) { this.approvedByTherapist = approvedByTherapist; return this; }
        public Builder previousPromptLevel(PromptLevel previousPromptLevel) { this.previousPromptLevel = previousPromptLevel; return this; }
        public Builder newPromptLevel(PromptLevel newPromptLevel) { this.newPromptLevel = newPromptLevel; return this; }
        public Builder changeDate(LocalDateTime changeDate) { this.changeDate = changeDate; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder subsequentPerformanceNotes(String subsequentPerformanceNotes) { this.subsequentPerformanceNotes = subsequentPerformanceNotes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PromptFadingHistory build() { return new PromptFadingHistory(id, child, step, approvedByTherapist, previousPromptLevel, newPromptLevel, changeDate, reason, subsequentPerformanceNotes, createdAt); }
    }

    @PrePersist
    protected void onCreate() {
        if (changeDate == null) {
            changeDate = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public User getApprovedByTherapist() { return approvedByTherapist; }
    public void setApprovedByTherapist(User approvedByTherapist) { this.approvedByTherapist = approvedByTherapist; }

    public PromptLevel getPreviousPromptLevel() { return previousPromptLevel; }
    public void setPreviousPromptLevel(PromptLevel previousPromptLevel) { this.previousPromptLevel = previousPromptLevel; }

    public PromptLevel getNewPromptLevel() { return newPromptLevel; }
    public void setNewPromptLevel(PromptLevel newPromptLevel) { this.newPromptLevel = newPromptLevel; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSubsequentPerformanceNotes() { return subsequentPerformanceNotes; }
    public void setSubsequentPerformanceNotes(String subsequentPerformanceNotes) { this.subsequentPerformanceNotes = subsequentPerformanceNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
