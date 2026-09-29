package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "home_programs")
public class HomeProgram {
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

    @Column(name = "frequency_per_week", nullable = false)
    private Integer frequencyPerWeek = 3;

    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes = 15;

    @ManyToOne
    @JoinColumn(name = "target_prompt_level_id", nullable = false)
    private PromptLevel targetPromptLevel;

    @Column(name = "goal_statement", columnDefinition = "TEXT")
    private String goalStatement;

    @Column(name = "caregiver_instructions", columnDefinition = "TEXT")
    private String caregiverInstructions;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToMany
    @JoinTable(
        name = "home_program_steps",
        joinColumns = @JoinColumn(name = "home_program_id"),
        inverseJoinColumns = @JoinColumn(name = "step_id")
    )
    private Set<TaskStep> targetSteps = new HashSet<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public HomeProgram() {}

    public HomeProgram(Long id, Child child, User therapist, Activity activity, Integer frequencyPerWeek, Integer targetDurationMinutes, PromptLevel targetPromptLevel, String goalStatement, String caregiverInstructions, LocalDate startDate, LocalDate endDate, Set<TaskStep> targetSteps, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.child = child;
        this.therapist = therapist;
        this.activity = activity;
        this.frequencyPerWeek = frequencyPerWeek != null ? frequencyPerWeek : 3;
        this.targetDurationMinutes = targetDurationMinutes != null ? targetDurationMinutes : 15;
        this.targetPromptLevel = targetPromptLevel;
        this.goalStatement = goalStatement;
        this.caregiverInstructions = caregiverInstructions;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetSteps = targetSteps != null ? targetSteps : new HashSet<>();
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private User therapist;
        private Activity activity;
        private Integer frequencyPerWeek = 3;
        private Integer targetDurationMinutes = 15;
        private PromptLevel targetPromptLevel;
        private String goalStatement;
        private String caregiverInstructions;
        private LocalDate startDate;
        private LocalDate endDate;
        private Set<TaskStep> targetSteps = new HashSet<>();
        private Boolean isActive = true;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder therapist(User therapist) { this.therapist = therapist; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder frequencyPerWeek(Integer frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; return this; }
        public Builder targetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; return this; }
        public Builder targetPromptLevel(PromptLevel targetPromptLevel) { this.targetPromptLevel = targetPromptLevel; return this; }
        public Builder goalStatement(String goalStatement) { this.goalStatement = goalStatement; return this; }
        public Builder caregiverInstructions(String caregiverInstructions) { this.caregiverInstructions = caregiverInstructions; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder targetSteps(Set<TaskStep> targetSteps) { this.targetSteps = targetSteps; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public HomeProgram build() { return new HomeProgram(id, child, therapist, activity, frequencyPerWeek, targetDurationMinutes, targetPromptLevel, goalStatement, caregiverInstructions, startDate, endDate, targetSteps, isActive, createdAt); }
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

    public Integer getFrequencyPerWeek() { return frequencyPerWeek; }
    public void setFrequencyPerWeek(Integer frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; }

    public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
    public void setTargetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; }

    public PromptLevel getTargetPromptLevel() { return targetPromptLevel; }
    public void setTargetPromptLevel(PromptLevel targetPromptLevel) { this.targetPromptLevel = targetPromptLevel; }

    public String getGoalStatement() { return goalStatement; }
    public void setGoalStatement(String goalStatement) { this.goalStatement = goalStatement; }

    public String getCaregiverInstructions() { return caregiverInstructions; }
    public void setCaregiverInstructions(String caregiverInstructions) { this.caregiverInstructions = caregiverInstructions; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Set<TaskStep> getTargetSteps() { return targetSteps; }
    public void setTargetSteps(Set<TaskStep> targetSteps) { this.targetSteps = targetSteps; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
