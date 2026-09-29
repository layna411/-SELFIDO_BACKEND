package com.simats.selfora.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "assessment_results")
public class AssessmentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private TaskStep step;

    @ManyToOne
    @JoinColumn(name = "baseline_prompt_level_id", nullable = false)
    private PromptLevel baselinePromptLevel;

    @Column(nullable = false, length = 30)
    private String outcome;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public AssessmentResult() {}

    public AssessmentResult(Long id, Assessment assessment, TaskStep step, PromptLevel baselinePromptLevel, String outcome, String notes) {
        this.id = id;
        this.assessment = assessment;
        this.step = step;
        this.baselinePromptLevel = baselinePromptLevel;
        this.outcome = outcome;
        this.notes = notes;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Assessment assessment;
        private TaskStep step;
        private PromptLevel baselinePromptLevel;
        private String outcome;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder assessment(Assessment assessment) { this.assessment = assessment; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder baselinePromptLevel(PromptLevel baselinePromptLevel) { this.baselinePromptLevel = baselinePromptLevel; return this; }
        public Builder outcome(String outcome) { this.outcome = outcome; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public AssessmentResult build() { return new AssessmentResult(id, assessment, step, baselinePromptLevel, outcome, notes); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public PromptLevel getBaselinePromptLevel() { return baselinePromptLevel; }
    public void setBaselinePromptLevel(PromptLevel baselinePromptLevel) { this.baselinePromptLevel = baselinePromptLevel; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
