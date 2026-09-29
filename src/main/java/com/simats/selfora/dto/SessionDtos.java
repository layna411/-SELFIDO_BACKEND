package com.simats.selfora.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SessionDtos {

    public static class StartSessionRequest {
        private Long childId;
        private Long activityId;
        private String sessionType;
        private String environment;

        public StartSessionRequest() {}

        public StartSessionRequest(Long childId, Long activityId, String sessionType, String environment) {
            this.childId = childId;
            this.activityId = activityId;
            this.sessionType = sessionType;
            this.environment = environment;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
    }

    public static class RecordPerformanceRequest {
        private Long stepId;
        private Integer promptLevelId;
        private String outcome;
        private Integer attempts;
        private Integer durationSeconds;
        private String therapistNote;
        private String caregiverNote;
        private String environment;

        public RecordPerformanceRequest() {}

        public RecordPerformanceRequest(Long stepId, Integer promptLevelId, String outcome, Integer attempts, Integer durationSeconds, String therapistNote, String caregiverNote, String environment) {
            this.stepId = stepId;
            this.promptLevelId = promptLevelId;
            this.outcome = outcome;
            this.attempts = attempts;
            this.durationSeconds = durationSeconds;
            this.therapistNote = therapistNote;
            this.caregiverNote = caregiverNote;
            this.environment = environment;
        }

        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }
        public Integer getPromptLevelId() { return promptLevelId; }
        public void setPromptLevelId(Integer promptLevelId) { this.promptLevelId = promptLevelId; }
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
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
    }

    public static class SessionResponse {
        private Long sessionId;
        private String sessionCode;
        private Long childId;
        private String childName;
        private Long conductedByUserId;
        private String conductedByUserName;
        private Long activityId;
        private String activityTitle;
        private String sessionType;
        private String environment;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalDurationSeconds;
        private String summaryNotes;
        private List<PerformanceRecordResponse> performanceRecords;

        public SessionResponse() {}

        public SessionResponse(Long sessionId, String sessionCode, Long childId, String childName, Long conductedByUserId, String conductedByUserName, Long activityId, String activityTitle, String sessionType, String environment, LocalDateTime startTime, LocalDateTime endTime, Integer totalDurationSeconds, String summaryNotes, List<PerformanceRecordResponse> performanceRecords) {
            this.sessionId = sessionId;
            this.sessionCode = sessionCode;
            this.childId = childId;
            this.childName = childName;
            this.conductedByUserId = conductedByUserId;
            this.conductedByUserName = conductedByUserName;
            this.activityId = activityId;
            this.activityTitle = activityTitle;
            this.sessionType = sessionType;
            this.environment = environment;
            this.startTime = startTime;
            this.endTime = endTime;
            this.totalDurationSeconds = totalDurationSeconds;
            this.summaryNotes = summaryNotes;
            this.performanceRecords = performanceRecords;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long sessionId;
            private String sessionCode;
            private Long childId;
            private String childName;
            private Long conductedByUserId;
            private String conductedByUserName;
            private Long activityId;
            private String activityTitle;
            private String sessionType;
            private String environment;
            private LocalDateTime startTime;
            private LocalDateTime endTime;
            private Integer totalDurationSeconds;
            private String summaryNotes;
            private List<PerformanceRecordResponse> performanceRecords;

            public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
            public Builder sessionCode(String sessionCode) { this.sessionCode = sessionCode; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder conductedByUserId(Long conductedByUserId) { this.conductedByUserId = conductedByUserId; return this; }
            public Builder conductedByUserName(String conductedByUserName) { this.conductedByUserName = conductedByUserName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder sessionType(String sessionType) { this.sessionType = sessionType; return this; }
            public Builder environment(String environment) { this.environment = environment; return this; }
            public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
            public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
            public Builder totalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; return this; }
            public Builder summaryNotes(String summaryNotes) { this.summaryNotes = summaryNotes; return this; }
            public Builder performanceRecords(List<PerformanceRecordResponse> performanceRecords) { this.performanceRecords = performanceRecords; return this; }
            public SessionResponse build() { return new SessionResponse(sessionId, sessionCode, childId, childName, conductedByUserId, conductedByUserName, activityId, activityTitle, sessionType, environment, startTime, endTime, totalDurationSeconds, summaryNotes, performanceRecords); }
        }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getSessionCode() { return sessionCode; }
        public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
        public Long getConductedByUserId() { return conductedByUserId; }
        public void setConductedByUserId(Long conductedByUserId) { this.conductedByUserId = conductedByUserId; }
        public String getConductedByUserName() { return conductedByUserName; }
        public void setConductedByUserName(String conductedByUserName) { this.conductedByUserName = conductedByUserName; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getActivityTitle() { return activityTitle; }
        public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
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
        public List<PerformanceRecordResponse> getPerformanceRecords() { return performanceRecords; }
        public void setPerformanceRecords(List<PerformanceRecordResponse> performanceRecords) { this.performanceRecords = performanceRecords; }
    }

    public static class PerformanceRecordResponse {
        private Long id;
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private Integer promptLevelId;
        private String promptLevelCode;
        private String promptLevelName;
        private String outcome;
        private Integer attempts;
        private Integer durationSeconds;
        private String therapistNote;
        private String caregiverNote;
        private LocalDateTime recordedAt;

        public PerformanceRecordResponse() {}

        public PerformanceRecordResponse(Long id, Long stepId, Integer stepNumber, String stepTitle, Integer promptLevelId, String promptLevelCode, String promptLevelName, String outcome, Integer attempts, Integer durationSeconds, String therapistNote, String caregiverNote, LocalDateTime recordedAt) {
            this.id = id;
            this.stepId = stepId;
            this.stepNumber = stepNumber;
            this.stepTitle = stepTitle;
            this.promptLevelId = promptLevelId;
            this.promptLevelCode = promptLevelCode;
            this.promptLevelName = promptLevelName;
            this.outcome = outcome;
            this.attempts = attempts;
            this.durationSeconds = durationSeconds;
            this.therapistNote = therapistNote;
            this.caregiverNote = caregiverNote;
            this.recordedAt = recordedAt;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long stepId;
            private Integer stepNumber;
            private String stepTitle;
            private Integer promptLevelId;
            private String promptLevelCode;
            private String promptLevelName;
            private String outcome;
            private Integer attempts;
            private Integer durationSeconds;
            private String therapistNote;
            private String caregiverNote;
            private LocalDateTime recordedAt;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder stepId(Long stepId) { this.stepId = stepId; return this; }
            public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
            public Builder stepTitle(String stepTitle) { this.stepTitle = stepTitle; return this; }
            public Builder promptLevelId(Integer promptLevelId) { this.promptLevelId = promptLevelId; return this; }
            public Builder promptLevelCode(String promptLevelCode) { this.promptLevelCode = promptLevelCode; return this; }
            public Builder promptLevelName(String promptLevelName) { this.promptLevelName = promptLevelName; return this; }
            public Builder outcome(String outcome) { this.outcome = outcome; return this; }
            public Builder attempts(Integer attempts) { this.attempts = attempts; return this; }
            public Builder durationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; return this; }
            public Builder therapistNote(String therapistNote) { this.therapistNote = therapistNote; return this; }
            public Builder caregiverNote(String caregiverNote) { this.caregiverNote = caregiverNote; return this; }
            public Builder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }
            public PerformanceRecordResponse build() { return new PerformanceRecordResponse(id, stepId, stepNumber, stepTitle, promptLevelId, promptLevelCode, promptLevelName, outcome, attempts, durationSeconds, therapistNote, caregiverNote, recordedAt); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }
        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
        public String getStepTitle() { return stepTitle; }
        public void setStepTitle(String stepTitle) { this.stepTitle = stepTitle; }
        public Integer getPromptLevelId() { return promptLevelId; }
        public void setPromptLevelId(Integer promptLevelId) { this.promptLevelId = promptLevelId; }
        public String getPromptLevelCode() { return promptLevelCode; }
        public void setPromptLevelCode(String promptLevelCode) { this.promptLevelCode = promptLevelCode; }
        public String getPromptLevelName() { return promptLevelName; }
        public void setPromptLevelName(String promptLevelName) { this.promptLevelName = promptLevelName; }
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

    public static class SessionSummaryResponse {
        private Long sessionId;
        private String sessionCode;
        private String childName;
        private String activityTitle;
        private Integer totalSteps;
        private Integer completedSteps;
        private Integer independentSteps;
        private Integer promptedSteps;
        private Integer unableSteps;
        private Double independencePercentage;
        private Integer totalDurationSeconds;
        private Map<String, Integer> promptDistribution;
        private String suggestedNextPromptLevel;
        private List<Long> suggestedFocusStepIds;

        public SessionSummaryResponse() {}

        public SessionSummaryResponse(Long sessionId, String sessionCode, String childName, String activityTitle, Integer totalSteps, Integer completedSteps, Integer independentSteps, Integer promptedSteps, Integer unableSteps, Double independencePercentage, Integer totalDurationSeconds, Map<String, Integer> promptDistribution, String suggestedNextPromptLevel, List<Long> suggestedFocusStepIds) {
            this.sessionId = sessionId;
            this.sessionCode = sessionCode;
            this.childName = childName;
            this.activityTitle = activityTitle;
            this.totalSteps = totalSteps;
            this.completedSteps = completedSteps;
            this.independentSteps = independentSteps;
            this.promptedSteps = promptedSteps;
            this.unableSteps = unableSteps;
            this.independencePercentage = independencePercentage;
            this.totalDurationSeconds = totalDurationSeconds;
            this.promptDistribution = promptDistribution;
            this.suggestedNextPromptLevel = suggestedNextPromptLevel;
            this.suggestedFocusStepIds = suggestedFocusStepIds;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long sessionId;
            private String sessionCode;
            private String childName;
            private String activityTitle;
            private Integer totalSteps;
            private Integer completedSteps;
            private Integer independentSteps;
            private Integer promptedSteps;
            private Integer unableSteps;
            private Double independencePercentage;
            private Integer totalDurationSeconds;
            private Map<String, Integer> promptDistribution;
            private String suggestedNextPromptLevel;
            private List<Long> suggestedFocusStepIds;

            public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
            public Builder sessionCode(String sessionCode) { this.sessionCode = sessionCode; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder totalSteps(Integer totalSteps) { this.totalSteps = totalSteps; return this; }
            public Builder completedSteps(Integer completedSteps) { this.completedSteps = completedSteps; return this; }
            public Builder independentSteps(Integer independentSteps) { this.independentSteps = independentSteps; return this; }
            public Builder promptedSteps(Integer promptedSteps) { this.promptedSteps = promptedSteps; return this; }
            public Builder unableSteps(Integer unableSteps) { this.unableSteps = unableSteps; return this; }
            public Builder independencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; return this; }
            public Builder totalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; return this; }
            public Builder promptDistribution(Map<String, Integer> promptDistribution) { this.promptDistribution = promptDistribution; return this; }
            public Builder suggestedNextPromptLevel(String suggestedNextPromptLevel) { this.suggestedNextPromptLevel = suggestedNextPromptLevel; return this; }
            public Builder suggestedFocusStepIds(List<Long> suggestedFocusStepIds) { this.suggestedFocusStepIds = suggestedFocusStepIds; return this; }
            public SessionSummaryResponse build() { return new SessionSummaryResponse(sessionId, sessionCode, childName, activityTitle, totalSteps, completedSteps, independentSteps, promptedSteps, unableSteps, independencePercentage, totalDurationSeconds, promptDistribution, suggestedNextPromptLevel, suggestedFocusStepIds); }
        }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getSessionCode() { return sessionCode; }
        public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
        public String getActivityTitle() { return activityTitle; }
        public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
        public Integer getTotalSteps() { return totalSteps; }
        public void setTotalSteps(Integer totalSteps) { this.totalSteps = totalSteps; }
        public Integer getCompletedSteps() { return completedSteps; }
        public void setCompletedSteps(Integer completedSteps) { this.completedSteps = completedSteps; }
        public Integer getIndependentSteps() { return independentSteps; }
        public void setIndependentSteps(Integer independentSteps) { this.independentSteps = independentSteps; }
        public Integer getPromptedSteps() { return promptedSteps; }
        public void setPromptedSteps(Integer promptedSteps) { this.promptedSteps = promptedSteps; }
        public Integer getUnableSteps() { return unableSteps; }
        public void setUnableSteps(Integer unableSteps) { this.unableSteps = unableSteps; }
        public Double getIndependencePercentage() { return independencePercentage; }
        public void setIndependencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; }
        public Integer getTotalDurationSeconds() { return totalDurationSeconds; }
        public void setTotalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }
        public Map<String, Integer> getPromptDistribution() { return promptDistribution; }
        public void setPromptDistribution(Map<String, Integer> promptDistribution) { this.promptDistribution = promptDistribution; }
        public String getSuggestedNextPromptLevel() { return suggestedNextPromptLevel; }
        public void setSuggestedNextPromptLevel(String suggestedNextPromptLevel) { this.suggestedNextPromptLevel = suggestedNextPromptLevel; }
        public List<Long> getSuggestedFocusStepIds() { return suggestedFocusStepIds; }
        public void setSuggestedFocusStepIds(List<Long> suggestedFocusStepIds) { this.suggestedFocusStepIds = suggestedFocusStepIds; }
    }
}
