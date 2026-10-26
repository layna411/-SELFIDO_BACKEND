package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "child_activity_progress", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cap_child_activity", columnNames = {"child_id", "activity_id"})
})
public class ChildActivityProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "last_step_number")
    private Integer lastStepNumber = 1;

    @Column(name = "total_steps_completed")
    private Integer totalStepsCompleted = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "stars_earned")
    private Integer starsEarned = 0;

    @Column(name = "last_practiced_at")
    private LocalDateTime lastPracticedAt;

    public ChildActivityProgress() {}

    public ChildActivityProgress(Child child, Activity activity, Integer lastStepNumber, Integer totalStepsCompleted, Boolean isCompleted, Integer starsEarned) {
        this.child = child;
        this.activity = activity;
        this.lastStepNumber = lastStepNumber;
        this.totalStepsCompleted = totalStepsCompleted;
        this.isCompleted = isCompleted;
        this.starsEarned = starsEarned;
        this.lastPracticedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        lastPracticedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public Integer getLastStepNumber() { return lastStepNumber; }
    public void setLastStepNumber(Integer lastStepNumber) { this.lastStepNumber = lastStepNumber; }

    public Integer getTotalStepsCompleted() { return totalStepsCompleted; }
    public void setTotalStepsCompleted(Integer totalStepsCompleted) { this.totalStepsCompleted = totalStepsCompleted; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Integer getStarsEarned() { return starsEarned; }
    public void setStarsEarned(Integer starsEarned) { this.starsEarned = starsEarned; }

    public LocalDateTime getLastPracticedAt() { return lastPracticedAt; }
    public void setLastPracticedAt(LocalDateTime lastPracticedAt) { this.lastPracticedAt = lastPracticedAt; }
}
