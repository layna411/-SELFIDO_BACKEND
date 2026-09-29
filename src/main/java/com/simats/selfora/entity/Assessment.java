package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments")
public class Assessment {
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

    @Column(name = "assessment_date", updatable = false)
    private LocalDateTime assessmentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Assessment() {}

    public Assessment(Long id, Child child, User therapist, Activity activity, LocalDateTime assessmentDate, String notes) {
        this.id = id;
        this.child = child;
        this.therapist = therapist;
        this.activity = activity;
        this.assessmentDate = assessmentDate;
        this.notes = notes;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private User therapist;
        private Activity activity;
        private LocalDateTime assessmentDate;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder therapist(User therapist) { this.therapist = therapist; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder assessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Assessment build() { return new Assessment(id, child, therapist, activity, assessmentDate, notes); }
    }

    @PrePersist
    protected void onCreate() {
        assessmentDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public User getTherapist() { return therapist; }
    public void setTherapist(User therapist) { this.therapist = therapist; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public LocalDateTime getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
