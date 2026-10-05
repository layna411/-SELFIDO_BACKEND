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

    // --- STAGE 5 DTOs ---

    public static class ActivityPerformanceCardDto {
        private String categoryName; // Dressing, Eating, Grooming, Shoes & Socks
        private Double currentIndependencePct;
        private Double previousIndependencePct;
        private String trend; // IMPROVING, STABLE, REGRESSING
        private Integer sessionsCount;

        public ActivityPerformanceCardDto() {}
        public ActivityPerformanceCardDto(String categoryName, Double currentIndependencePct, Double previousIndependencePct, String trend, Integer sessionsCount) {
            this.categoryName = categoryName;
            this.currentIndependencePct = currentIndependencePct;
            this.previousIndependencePct = previousIndependencePct;
            this.trend = trend;
            this.sessionsCount = sessionsCount;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Double getCurrentIndependencePct() { return currentIndependencePct; }
        public void setCurrentIndependencePct(Double currentIndependencePct) { this.currentIndependencePct = currentIndependencePct; }
        public Double getPreviousIndependencePct() { return previousIndependencePct; }
        public void setPreviousIndependencePct(Double previousIndependencePct) { this.previousIndependencePct = previousIndependencePct; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
        public Integer getSessionsCount() { return sessionsCount; }
        public void setSessionsCount(Integer sessionsCount) { this.sessionsCount = sessionsCount; }
    }

    public static class DifficultStepDto {
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private String activityTitle;
        private String mainAssistanceLevel;
        private Integer physicalAssistanceCount;
        private Integer verbalPromptCount;
        private Integer unableCount;
        private String reasonDescription;

        public DifficultStepDto() {}
        public DifficultStepDto(Long stepId, Integer stepNumber, String stepTitle, String activityTitle, String mainAssistanceLevel, Integer physicalAssistanceCount, Integer verbalPromptCount, Integer unableCount, String reasonDescription) {
            this.stepId = stepId;
            this.stepNumber = stepNumber;
            this.stepTitle = stepTitle;
            this.activityTitle = activityTitle;
            this.mainAssistanceLevel = mainAssistanceLevel;
            this.physicalAssistanceCount = physicalAssistanceCount;
            this.verbalPromptCount = verbalPromptCount;
            this.unableCount = unableCount;
            this.reasonDescription = reasonDescription;
        }

        public Long getStepId() { return stepId; }
        public Integer getStepNumber() { return stepNumber; }
        public String getStepTitle() { return stepTitle; }
        public String getActivityTitle() { return activityTitle; }
        public String getMainAssistanceLevel() { return mainAssistanceLevel; }
        public Integer getPhysicalAssistanceCount() { return physicalAssistanceCount; }
        public Integer getVerbalPromptCount() { return verbalPromptCount; }
        public Integer getUnableCount() { return unableCount; }
        public String getReasonDescription() { return reasonDescription; }
    }

    public static class IndependentStepDto {
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private String activityTitle;
        private Integer consecutiveIndependentSessions;
        private String generalizationReadiness;

        public IndependentStepDto() {}
        public IndependentStepDto(Long stepId, Integer stepNumber, String stepTitle, String activityTitle, Integer consecutiveIndependentSessions, String generalizationReadiness) {
            this.stepId = stepId;
            this.stepNumber = stepNumber;
            this.stepTitle = stepTitle;
            this.activityTitle = activityTitle;
            this.consecutiveIndependentSessions = consecutiveIndependentSessions;
            this.generalizationReadiness = generalizationReadiness;
        }

        public Long getStepId() { return stepId; }
        public Integer getStepNumber() { return stepNumber; }
        public String getStepTitle() { return stepTitle; }
        public String getActivityTitle() { return activityTitle; }
        public Integer getConsecutiveIndependentSessions() { return consecutiveIndependentSessions; }
        public String getGeneralizationReadiness() { return generalizationReadiness; }
    }

    public static class SessionComparisonDto {
        private String currentSessionDate;
        private String previousSessionDate;
        private Double currentIndependencePercentage;
        private Double previousIndependencePercentage;
        private List<String> improvedSteps;
        private List<String> regressedSteps;
        private List<String> unchangedSteps;

        public SessionComparisonDto() {}
        public SessionComparisonDto(String currentSessionDate, String previousSessionDate, Double currentIndependencePercentage, Double previousIndependencePercentage, List<String> improvedSteps, List<String> regressedSteps, List<String> unchangedSteps) {
            this.currentSessionDate = currentSessionDate;
            this.previousSessionDate = previousSessionDate;
            this.currentIndependencePercentage = currentIndependencePercentage;
            this.previousIndependencePercentage = previousIndependencePercentage;
            this.improvedSteps = improvedSteps;
            this.regressedSteps = regressedSteps;
            this.unchangedSteps = unchangedSteps;
        }

        public String getCurrentSessionDate() { return currentSessionDate; }
        public String getPreviousSessionDate() { return previousSessionDate; }
        public Double getCurrentIndependencePercentage() { return currentIndependencePercentage; }
        public Double getPreviousIndependencePercentage() { return previousIndependencePercentage; }
        public List<String> getImprovedSteps() { return improvedSteps; }
        public List<String> getRegressedSteps() { return regressedSteps; }
        public List<String> getUnchangedSteps() { return unchangedSteps; }
    }

    public static class RecommendationDto {
        private String triggeringEvidence;
        private String sessionDates;
        private String activityTitle;
        private String stepTitle;
        private String suggestedTherapistAction;

        public RecommendationDto() {}
        public RecommendationDto(String triggeringEvidence, String sessionDates, String activityTitle, String stepTitle, String suggestedTherapistAction) {
            this.triggeringEvidence = triggeringEvidence;
            this.sessionDates = sessionDates;
            this.activityTitle = activityTitle;
            this.stepTitle = stepTitle;
            this.suggestedTherapistAction = suggestedTherapistAction;
        }

        public String getTriggeringEvidence() { return triggeringEvidence; }
        public String getSessionDates() { return sessionDates; }
        public String getActivityTitle() { return activityTitle; }
        public String getStepTitle() { return stepTitle; }
        public String getSuggestedTherapistAction() { return suggestedTherapistAction; }
    }

    public static class CaregiverProgressSummaryDto {
        private Integer totalSubmissions;
        private Integer completedActivitiesCount;
        private String reportedOutcomeSummary;
        private String participationFrequency;
        private Boolean distinctFromClinicalScore = true;

        public CaregiverProgressSummaryDto() {}
        public CaregiverProgressSummaryDto(Integer totalSubmissions, Integer completedActivitiesCount, String reportedOutcomeSummary, String participationFrequency, Boolean distinctFromClinicalScore) {
            this.totalSubmissions = totalSubmissions;
            this.completedActivitiesCount = completedActivitiesCount;
            this.reportedOutcomeSummary = reportedOutcomeSummary;
            this.participationFrequency = participationFrequency;
            this.distinctFromClinicalScore = distinctFromClinicalScore != null ? distinctFromClinicalScore : true;
        }

        public Integer getTotalSubmissions() { return totalSubmissions; }
        public Integer getCompletedActivitiesCount() { return completedActivitiesCount; }
        public String getReportedOutcomeSummary() { return reportedOutcomeSummary; }
        public String getParticipationFrequency() { return participationFrequency; }
        public Boolean getDistinctFromClinicalScore() { return distinctFromClinicalScore; }
    }

    public static class AnalyzeAndAdaptDashboardResponse {
        private Long childId;
        private String childName;
        private Long selectedActivityId;
        private String selectedActivityTitle;
        private Integer totalRecordedSessions;
        private String latestAssessmentDate;
        private String previousAssessmentDate;
        private Double currentIndependencePercentage;
        private Double previousIndependencePercentage;
        private String overallTrend; // IMPROVING, STABLE, REGRESSING
        private List<ActivityPerformanceCardDto> activityCards;
        private List<DifficultStepDto> difficultSteps;
        private List<IndependentStepDto> independentSteps;
        private SessionComparisonDto sessionComparison;
        private List<String> factualObservations;
        private List<RecommendationDto> recommendations;
        private CaregiverProgressSummaryDto caregiverProgressSummary;

        public AnalyzeAndAdaptDashboardResponse() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long childId;
            private String childName;
            private Long selectedActivityId;
            private String selectedActivityTitle;
            private Integer totalRecordedSessions;
            private String latestAssessmentDate;
            private String previousAssessmentDate;
            private Double currentIndependencePercentage;
            private Double previousIndependencePercentage;
            private String overallTrend;
            private List<ActivityPerformanceCardDto> activityCards;
            private List<DifficultStepDto> difficultSteps;
            private List<IndependentStepDto> independentSteps;
            private SessionComparisonDto sessionComparison;
            private List<String> factualObservations;
            private List<RecommendationDto> recommendations;
            private CaregiverProgressSummaryDto caregiverProgressSummary;

            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder selectedActivityId(Long selectedActivityId) { this.selectedActivityId = selectedActivityId; return this; }
            public Builder selectedActivityTitle(String selectedActivityTitle) { this.selectedActivityTitle = selectedActivityTitle; return this; }
            public Builder totalRecordedSessions(Integer totalRecordedSessions) { this.totalRecordedSessions = totalRecordedSessions; return this; }
            public Builder latestAssessmentDate(String latestAssessmentDate) { this.latestAssessmentDate = latestAssessmentDate; return this; }
            public Builder previousAssessmentDate(String previousAssessmentDate) { this.previousAssessmentDate = previousAssessmentDate; return this; }
            public Builder currentIndependencePercentage(Double currentIndependencePercentage) { this.currentIndependencePercentage = currentIndependencePercentage; return this; }
            public Builder previousIndependencePercentage(Double previousIndependencePercentage) { this.previousIndependencePercentage = previousIndependencePercentage; return this; }
            public Builder overallTrend(String overallTrend) { this.overallTrend = overallTrend; return this; }
            public Builder activityCards(List<ActivityPerformanceCardDto> activityCards) { this.activityCards = activityCards; return this; }
            public Builder difficultSteps(List<DifficultStepDto> difficultSteps) { this.difficultSteps = difficultSteps; return this; }
            public Builder independentSteps(List<IndependentStepDto> independentSteps) { this.independentSteps = independentSteps; return this; }
            public Builder sessionComparison(SessionComparisonDto sessionComparison) { this.sessionComparison = sessionComparison; return this; }
            public Builder factualObservations(List<String> factualObservations) { this.factualObservations = factualObservations; return this; }
            public Builder recommendations(List<RecommendationDto> recommendations) { this.recommendations = recommendations; return this; }
            public Builder caregiverProgressSummary(CaregiverProgressSummaryDto caregiverProgressSummary) { this.caregiverProgressSummary = caregiverProgressSummary; return this; }

            public AnalyzeAndAdaptDashboardResponse build() {
                AnalyzeAndAdaptDashboardResponse res = new AnalyzeAndAdaptDashboardResponse();
                res.childId = childId;
                res.childName = childName;
                res.selectedActivityId = selectedActivityId;
                res.selectedActivityTitle = selectedActivityTitle;
                res.totalRecordedSessions = totalRecordedSessions;
                res.latestAssessmentDate = latestAssessmentDate;
                res.previousAssessmentDate = previousAssessmentDate;
                res.currentIndependencePercentage = currentIndependencePercentage;
                res.previousIndependencePercentage = previousIndependencePercentage;
                res.overallTrend = overallTrend;
                res.activityCards = activityCards;
                res.difficultSteps = difficultSteps;
                res.independentSteps = independentSteps;
                res.sessionComparison = sessionComparison;
                res.factualObservations = factualObservations;
                res.recommendations = recommendations;
                res.caregiverProgressSummary = caregiverProgressSummary;
                return res;
            }
        }

        public Long getChildId() { return childId; }
        public String getChildName() { return childName; }
        public Long getSelectedActivityId() { return selectedActivityId; }
        public String getSelectedActivityTitle() { return selectedActivityTitle; }
        public Integer getTotalRecordedSessions() { return totalRecordedSessions; }
        public String getLatestAssessmentDate() { return latestAssessmentDate; }
        public String getPreviousAssessmentDate() { return previousAssessmentDate; }
        public Double getCurrentIndependencePercentage() { return currentIndependencePercentage; }
        public Double getPreviousIndependencePercentage() { return previousIndependencePercentage; }
        public String getOverallTrend() { return overallTrend; }
        public List<ActivityPerformanceCardDto> getActivityCards() { return activityCards; }
        public List<DifficultStepDto> getDifficultSteps() { return difficultSteps; }
        public List<IndependentStepDto> getIndependentSteps() { return independentSteps; }
        public SessionComparisonDto getSessionComparison() { return sessionComparison; }
        public List<String> getFactualObservations() { return factualObservations; }
        public List<RecommendationDto> getRecommendations() { return recommendations; }
        public CaregiverProgressSummaryDto getCaregiverProgressSummary() { return caregiverProgressSummary; }
    }
}
