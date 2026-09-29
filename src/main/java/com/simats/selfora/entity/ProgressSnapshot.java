package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress_snapshots")
public class ProgressSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "independence_percentage", nullable = false)
    private Double independencePercentage;

    @Column(name = "prompt_distribution_json", nullable = false, columnDefinition = "JSON")
    private String promptDistributionJson;

    @Column(name = "steps_total", nullable = false)
    private Integer stepsTotal;

    @Column(name = "steps_independent", nullable = false)
    private Integer stepsIndependent;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ProgressSnapshot() {}

    public ProgressSnapshot(Long id, Child child, Activity activity, LocalDate snapshotDate, Double independencePercentage, String promptDistributionJson, Integer stepsTotal, Integer stepsIndependent, LocalDateTime createdAt) {
        this.id = id;
        this.child = child;
        this.activity = activity;
        this.snapshotDate = snapshotDate;
        this.independencePercentage = independencePercentage;
        this.promptDistributionJson = promptDistributionJson;
        this.stepsTotal = stepsTotal;
        this.stepsIndependent = stepsIndependent;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private Activity activity;
        private LocalDate snapshotDate;
        private Double independencePercentage;
        private String promptDistributionJson;
        private Integer stepsTotal;
        private Integer stepsIndependent;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder snapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; return this; }
        public Builder independencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; return this; }
        public Builder promptDistributionJson(String promptDistributionJson) { this.promptDistributionJson = promptDistributionJson; return this; }
        public Builder stepsTotal(Integer stepsTotal) { this.stepsTotal = stepsTotal; return this; }
        public Builder stepsIndependent(Integer stepsIndependent) { this.stepsIndependent = stepsIndependent; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProgressSnapshot build() { return new ProgressSnapshot(id, child, activity, snapshotDate, independencePercentage, promptDistributionJson, stepsTotal, stepsIndependent, createdAt); }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }

    public Double getIndependencePercentage() { return independencePercentage; }
    public void setIndependencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; }

    public String getPromptDistributionJson() { return promptDistributionJson; }
    public void setPromptDistributionJson(String promptDistributionJson) { this.promptDistributionJson = promptDistributionJson; }

    public Integer getStepsTotal() { return stepsTotal; }
    public void setStepsTotal(Integer stepsTotal) { this.stepsTotal = stepsTotal; }

    public Integer getStepsIndependent() { return stepsIndependent; }
    public void setStepsIndependent(Integer stepsIndependent) { this.stepsIndependent = stepsIndependent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
