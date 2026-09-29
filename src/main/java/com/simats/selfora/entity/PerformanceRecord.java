package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_records")
public class PerformanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TherapySession session;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private TaskStep step;

    @ManyToOne
    @JoinColumn(name = "prompt_level_id", nullable = false)
    private PromptLevel promptLevel;

    @Column(nullable = false, length = 30)
    private String outcome;

    @Column(nullable = false)
    private Integer attempts = 1;

    @Column(name = "duration_seconds")
    private Integer durationSeconds = 0;

    @Column(name = "therapist_note", columnDefinition = "TEXT")
    private String therapistNote;

    @Column(name = "caregiver_note", columnDefinition = "TEXT")
    private String caregiverNote;

    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public PerformanceRecord() {}

    public PerformanceRecord(Long id, TherapySession session, TaskStep step, PromptLevel promptLevel, String outcome, Integer attempts, Integer durationSeconds, String therapistNote, String caregiverNote, LocalDateTime recordedAt) {
        this.id = id;
        this.session = session;
        this.step = step;
        this.promptLevel = promptLevel;
        this.outcome = outcome;
        this.attempts = attempts != null ? attempts : 1;
        this.durationSeconds = durationSeconds != null ? durationSeconds : 0;
        this.therapistNote = therapistNote;
        this.caregiverNote = caregiverNote;
        this.recordedAt = recordedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private TherapySession session;
        private TaskStep step;
        private PromptLevel promptLevel;
        private String outcome;
        private Integer attempts = 1;
        private Integer durationSeconds = 0;
        private String therapistNote;
        private String caregiverNote;
        private LocalDateTime recordedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder session(TherapySession session) { this.session = session; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder promptLevel(PromptLevel promptLevel) { this.promptLevel = promptLevel; return this; }
        public Builder outcome(String outcome) { this.outcome = outcome; return this; }
        public Builder attempts(Integer attempts) { this.attempts = attempts; return this; }
        public Builder durationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; return this; }
        public Builder therapistNote(String therapistNote) { this.therapistNote = therapistNote; return this; }
        public Builder caregiverNote(String caregiverNote) { this.caregiverNote = caregiverNote; return this; }
        public Builder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }
        public PerformanceRecord build() { return new PerformanceRecord(id, session, step, promptLevel, outcome, attempts, durationSeconds, therapistNote, caregiverNote, recordedAt); }
    }

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TherapySession getSession() { return session; }
    public void setSession(TherapySession session) { this.session = session; }

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public PromptLevel getPromptLevel() { return promptLevel; }
    public void setPromptLevel(PromptLevel promptLevel) { this.promptLevel = promptLevel; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getTherapistNote() { return therapistNote; }
    public void setTherapistNote(String therapistNote) { this.therapistNote = therapistNote; }

    public String getCaregiverNote() { return caregiverNote; }
    public void setCaregiverNote(String caregiverNote) { this.caregiverNote = caregiverNote; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
