package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "therapy_sessions")
public class TherapySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_code", nullable = false, unique = true, length = 100)
    private String sessionCode;

    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @JoinColumn(name = "conducted_by_user_id", nullable = false)
    private User conductedByUser;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "session_type", nullable = false, length = 30)
    private String sessionType;

    @Column(length = 50)
    private String environment = "CLINIC";

    @Column(name = "start_time", updatable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_duration_seconds")
    private Integer totalDurationSeconds = 0;

    @Column(name = "summary_notes", columnDefinition = "TEXT")
    private String summaryNotes;

    public TherapySession() {}

    public TherapySession(Long id, String sessionCode, Child child, User conductedByUser, Activity activity, String sessionType, String environment, LocalDateTime startTime, LocalDateTime endTime, Integer totalDurationSeconds, String summaryNotes) {
        this.id = id;
        this.sessionCode = sessionCode;
        this.child = child;
        this.conductedByUser = conductedByUser;
        this.activity = activity;
        this.sessionType = sessionType;
        this.environment = environment != null ? environment : "CLINIC";
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalDurationSeconds = totalDurationSeconds != null ? totalDurationSeconds : 0;
        this.summaryNotes = summaryNotes;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String sessionCode;
        private Child child;
        private User conductedByUser;
        private Activity activity;
        private String sessionType;
        private String environment = "CLINIC";
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalDurationSeconds = 0;
        private String summaryNotes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder sessionCode(String sessionCode) { this.sessionCode = sessionCode; return this; }
        public Builder child(Child child) { this.child = child; return this; }
        public Builder conductedByUser(User conductedByUser) { this.conductedByUser = conductedByUser; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder sessionType(String sessionType) { this.sessionType = sessionType; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder totalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; return this; }
        public Builder summaryNotes(String summaryNotes) { this.summaryNotes = summaryNotes; return this; }
        public TherapySession build() { return new TherapySession(id, sessionCode, child, conductedByUser, activity, sessionType, environment, startTime, endTime, totalDurationSeconds, summaryNotes); }
    }

    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }

    public User getConductedByUser() { return conductedByUser; }
    public void setConductedByUser(User conductedByUser) { this.conductedByUser = conductedByUser; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }

    public String getSummaryNotes() { return summaryNotes; }
    public void setSummaryNotes(String summaryNotes) { this.summaryNotes = summaryNotes; }
}
