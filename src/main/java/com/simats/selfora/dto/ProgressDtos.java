package com.simats.selfora.dto;

import java.util.List;
import java.util.Map;

public class ProgressDtos {

    public static class StepProgressItem {
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private String currentPromptLevel;
        private Double independencePercentage;
        private Integer totalAttempts;

        public StepProgressItem() {}

        public StepProgressItem(Long stepId, Integer stepNumber, String stepTitle, String currentPromptLevel, Double independencePercentage, Integer totalAttempts) {
            this.stepId = stepId;
            this.stepNumber = stepNumber;
            this.stepTitle = stepTitle;
            this.currentPromptLevel = currentPromptLevel;
            this.independencePercentage = independencePercentage;
            this.totalAttempts = totalAttempts;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long stepId;
            private Integer stepNumber;
            private String stepTitle;
            private String currentPromptLevel;
            private Double independencePercentage;
            private Integer totalAttempts;

            public Builder stepId(Long stepId) { this.stepId = stepId; return this; }
            public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
            public Builder stepTitle(String stepTitle) { this.stepTitle = stepTitle; return this; }
            public Builder currentPromptLevel(String currentPromptLevel) { this.currentPromptLevel = currentPromptLevel; return this; }
            public Builder independencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; return this; }
            public Builder totalAttempts(Integer totalAttempts) { this.totalAttempts = totalAttempts; return this; }
            public StepProgressItem build() { return new StepProgressItem(stepId, stepNumber, stepTitle, currentPromptLevel, independencePercentage, totalAttempts); }
        }

        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }
        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
        public String getStepTitle() { return stepTitle; }
        public void setStepTitle(String stepTitle) { this.stepTitle = stepTitle; }
        public String getCurrentPromptLevel() { return currentPromptLevel; }
        public void setCurrentPromptLevel(String currentPromptLevel) { this.currentPromptLevel = currentPromptLevel; }
        public Double getIndependencePercentage() { return independencePercentage; }
        public void setIndependencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; }
        public Integer getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(Integer totalAttempts) { this.totalAttempts = totalAttempts; }
    }

    public static class EnvironmentProgressItem {
        private String environment;
        private Double independencePercentage;
        private String dominantPromptLevel;

        public EnvironmentProgressItem() {}

        public EnvironmentProgressItem(String environment, Double independencePercentage, String dominantPromptLevel) {
            this.environment = environment;
            this.independencePercentage = independencePercentage;
            this.dominantPromptLevel = dominantPromptLevel;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String environment;
            private Double independencePercentage;
            private String dominantPromptLevel;

            public Builder environment(String environment) { this.environment = environment; return this; }
            public Builder independencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; return this; }
            public Builder dominantPromptLevel(String dominantPromptLevel) { this.dominantPromptLevel = dominantPromptLevel; return this; }
            public EnvironmentProgressItem build() { return new EnvironmentProgressItem(environment, independencePercentage, dominantPromptLevel); }
        }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public Double getIndependencePercentage() { return independencePercentage; }
        public void setIndependencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; }
        public String getDominantPromptLevel() { return dominantPromptLevel; }
        public void setDominantPromptLevel(String dominantPromptLevel) { this.dominantPromptLevel = dominantPromptLevel; }
    }

    public static class SessionPoint {
        private Long sessionId;
        private String sessionDate;
        private Double independencePercentage;
        private String sessionType;

        public SessionPoint() {}

        public SessionPoint(Long sessionId, String sessionDate, Double independencePercentage, String sessionType) {
            this.sessionId = sessionId;
            this.sessionDate = sessionDate;
            this.independencePercentage = independencePercentage;
            this.sessionType = sessionType;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long sessionId;
            private String sessionDate;
            private Double independencePercentage;
            private String sessionType;

            public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
            public Builder sessionDate(String sessionDate) { this.sessionDate = sessionDate; return this; }
            public Builder independencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; return this; }
            public Builder sessionType(String sessionType) { this.sessionType = sessionType; return this; }
            public SessionPoint build() { return new SessionPoint(sessionId, sessionDate, independencePercentage, sessionType); }
        }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getSessionDate() { return sessionDate; }
        public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }
        public Double getIndependencePercentage() { return independencePercentage; }
        public void setIndependencePercentage(Double independencePercentage) { this.independencePercentage = independencePercentage; }
        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    }

    public static class ProgressSummaryResponse {
        private Long childId;
        private String childName;
        private Long activityId;
        private String activityTitle;
        private Double overallIndependencePercentage;
        private Integer totalSessionsCompleted;
        private Map<String, Integer> promptDistribution;
        private List<StepProgressItem> stepProgress;
        private List<EnvironmentProgressItem> generalizationProgress;
        private List<SessionPoint> historyPoints;

        public ProgressSummaryResponse() {}

        public ProgressSummaryResponse(Long childId, String childName, Long activityId, String activityTitle, Double overallIndependencePercentage, Integer totalSessionsCompleted, Map<String, Integer> promptDistribution, List<StepProgressItem> stepProgress, List<EnvironmentProgressItem> generalizationProgress, List<SessionPoint> historyPoints) {
            this.childId = childId;
            this.childName = childName;
            this.activityId = activityId;
            this.activityTitle = activityTitle;
            this.overallIndependencePercentage = overallIndependencePercentage;
            this.totalSessionsCompleted = totalSessionsCompleted;
            this.promptDistribution = promptDistribution;
            this.stepProgress = stepProgress;
            this.generalizationProgress = generalizationProgress;
            this.historyPoints = historyPoints;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long childId;
            private String childName;
            private Long activityId;
            private String activityTitle;
            private Double overallIndependencePercentage;
            private Integer totalSessionsCompleted;
            private Map<String, Integer> promptDistribution;
            private List<StepProgressItem> stepProgress;
            private List<EnvironmentProgressItem> generalizationProgress;
            private List<SessionPoint> historyPoints;

            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder overallIndependencePercentage(Double overallIndependencePercentage) { this.overallIndependencePercentage = overallIndependencePercentage; return this; }
            public Builder totalSessionsCompleted(Integer totalSessionsCompleted) { this.totalSessionsCompleted = totalSessionsCompleted; return this; }
            public Builder promptDistribution(Map<String, Integer> promptDistribution) { this.promptDistribution = promptDistribution; return this; }
            public Builder stepProgress(List<StepProgressItem> stepProgress) { this.stepProgress = stepProgress; return this; }
            public Builder generalizationProgress(List<EnvironmentProgressItem> generalizationProgress) { this.generalizationProgress = generalizationProgress; return this; }
            public Builder historyPoints(List<SessionPoint> historyPoints) { this.historyPoints = historyPoints; return this; }
            public ProgressSummaryResponse build() { return new ProgressSummaryResponse(childId, childName, activityId, activityTitle, overallIndependencePercentage, totalSessionsCompleted, promptDistribution, stepProgress, generalizationProgress, historyPoints); }
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getActivityTitle() { return activityTitle; }
        public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
        public Double getOverallIndependencePercentage() { return overallIndependencePercentage; }
        public void setOverallIndependencePercentage(Double overallIndependencePercentage) { this.overallIndependencePercentage = overallIndependencePercentage; }
        public Integer getTotalSessionsCompleted() { return totalSessionsCompleted; }
        public void setTotalSessionsCompleted(Integer totalSessionsCompleted) { this.totalSessionsCompleted = totalSessionsCompleted; }
        public Map<String, Integer> getPromptDistribution() { return promptDistribution; }
        public void setPromptDistribution(Map<String, Integer> promptDistribution) { this.promptDistribution = promptDistribution; }
        public List<StepProgressItem> getStepProgress() { return stepProgress; }
        public void setStepProgress(List<StepProgressItem> stepProgress) { this.stepProgress = stepProgress; }
        public List<EnvironmentProgressItem> getGeneralizationProgress() { return generalizationProgress; }
        public void setGeneralizationProgress(List<EnvironmentProgressItem> generalizationProgress) { this.generalizationProgress = generalizationProgress; }
        public List<SessionPoint> getHistoryPoints() { return historyPoints; }
        public void setHistoryPoints(List<SessionPoint> historyPoints) { this.historyPoints = historyPoints; }
    }
}
