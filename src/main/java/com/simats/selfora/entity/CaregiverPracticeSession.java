package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "caregiver_practice_sessions")
public class CaregiverPracticeSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    @ManyToOne
    @JoinColumn(name = "home_program_id", nullable = false)
    private HomeProgram homeProgram;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "practice_date", nullable = false)
    private LocalDate practiceDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 15;

    @Column(name = "caregiver_notes", columnDefinition = "TEXT")
    private String caregiverNotes;

    @Column(nullable = false, length = 30)
    private String status = "SUBMITTED"; // DRAFT, SUBMITTED, REVIEWED

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @ManyToOne
    @JoinColumn(name = "reviewing_therapist_id")
    private User reviewingTherapist;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "therapist_feedback", columnDefinition = "TEXT")
    private String therapistFeedback;

    @Column(name = "flagged_for_followup")
    private Boolean flaggedForFollowup = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public CaregiverPracticeSession() {}

    public CaregiverPracticeSession(Long id, Child child, User caregiver, HomeProgram homeProgram, Activity activity, LocalDate practiceDate, Integer durationMinutes, String caregiverNotes, String status, LocalDateTime submittedAt, User reviewingTherapist, LocalDateTime reviewedAt, String therapistFeedback, Boolean flaggedForFollowup, LocalDateTime createdAt) {
        this.id = id;
        this.child = child;
        this.caregiver = caregiver;
        this.homeProgram = homeProgram;
        this.activity = activity;
        this.practiceDate = practiceDate;
        this.durationMinutes = durationMinutes != null ? durationMinutes : 15;
        this.caregiverNotes = caregiverNotes;
        this.status = status != null ? status : "SUBMITTED";
        this.submittedAt = submittedAt;
        this.reviewingTherapist = reviewingTherapist;
        this.reviewedAt = reviewedAt;
        this.therapistFeedback = therapistFeedback;
        this.flaggedForFollowup = flaggedForFollowup != null ? flaggedForFollowup : false;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Child child;
        private User caregiver;
        private HomeProgram homeProgram;
        private Activity activity;
        private LocalDate practiceDate;
        private Integer durationMinutes = 15;
        private String caregiverNotes;
        private String status = "SUBMITTED";
        private LocalDateTime submittedAt;
        private User reviewingTherapist;
        private LocalDateTime reviewedAt;
        private String therapistFeedback;
        private Boolean flaggedForFollowup = false;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder caregiver(User caregiver) { this.caregiver = caregiver; return this; }
        public Builder homeProgram(HomeProgram homeProgram) { this.homeProgram = homeProgram; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder practiceDate(LocalDate practiceDate) { this.practiceDate = practiceDate; return this; }
        public Builder durationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder caregiverNotes(String caregiverNotes) { this.caregiverNotes = caregiverNotes; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public Builder reviewingTherapist(User reviewingTherapist) { this.reviewingTherapist = reviewingTherapist; return this; }
        public Builder reviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; return this; }
        public Builder therapistFeedback(String therapistFeedback) { this.therapistFeedback = therapistFeedback; return this; }
        public Builder flaggedForFollowup(Boolean flaggedForFollowup) { this.flaggedForFollowup = flaggedForFollowup; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public CaregiverPracticeSession build() {
            return new CaregiverPracticeSession(id, child, caregiver, homeProgram, activity, practiceDate, durationMinutes, caregiverNotes, status, submittedAt, reviewingTherapist, reviewedAt, therapistFeedback, flaggedForFollowup, createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) submittedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public User getCaregiver() { return caregiver; }
    public void setCaregiver(User caregiver) { this.caregiver = caregiver; }

    public HomeProgram getHomeProgram() { return homeProgram; }
    public void setHomeProgram(HomeProgram homeProgram) { this.homeProgram = homeProgram; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public LocalDate getPracticeDate() { return practiceDate; }
    public void setPracticeDate(LocalDate practiceDate) { this.practiceDate = practiceDate; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getCaregiverNotes() { return caregiverNotes; }
    public void setCaregiverNotes(String caregiverNotes) { this.caregiverNotes = caregiverNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public User getReviewingTherapist() { return reviewingTherapist; }
    public void setReviewingTherapist(User reviewingTherapist) { this.reviewingTherapist = reviewingTherapist; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getTherapistFeedback() { return therapistFeedback; }
    public void setTherapistFeedback(String therapistFeedback) { this.therapistFeedback = therapistFeedback; }

    public Boolean getFlaggedForFollowup() { return flaggedForFollowup; }
    public void setFlaggedForFollowup(Boolean flaggedForFollowup) { this.flaggedForFollowup = flaggedForFollowup; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
