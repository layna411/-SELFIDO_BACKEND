package com.simats.selfora.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "caregiver_practice_step_results")
public class CaregiverPracticeStepResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "practice_session_id", nullable = false)
    private CaregiverPracticeSession practiceSession;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private TaskStep step;

    @Column(nullable = false, length = 50)
    private String outcome; // INDEPENDENT, LITTLE_HELP, LOT_HELP, COULD_NOT_COMPLETE, NOT_PRACTISED

    @Column(columnDefinition = "TEXT")
    private String observation;

    public CaregiverPracticeStepResult() {}

    public CaregiverPracticeStepResult(Long id, CaregiverPracticeSession practiceSession, TaskStep step, String outcome, String observation) {
        this.id = id;
        this.practiceSession = practiceSession;
        this.step = step;
        this.outcome = outcome;
        this.observation = observation;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private CaregiverPracticeSession practiceSession;
        private TaskStep step;
        private String outcome;
        private String observation;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder practiceSession(CaregiverPracticeSession practiceSession) { this.practiceSession = practiceSession; return this; }
        public Builder step(TaskStep step) { this.step = step; return this; }
        public Builder outcome(String outcome) { this.outcome = outcome; return this; }
        public Builder observation(String observation) { this.observation = observation; return this; }

        public CaregiverPracticeStepResult build() {
            return new CaregiverPracticeStepResult(id, practiceSession, step, outcome, observation);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CaregiverPracticeSession getPracticeSession() { return practiceSession; }
    public void setPracticeSession(CaregiverPracticeSession practiceSession) { this.practiceSession = practiceSession; }

    public TaskStep getStep() { return step; }
    public void setStep(TaskStep step) { this.step = step; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
