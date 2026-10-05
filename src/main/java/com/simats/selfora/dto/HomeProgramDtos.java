package com.simats.selfora.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HomeProgramDtos {

    public static class CreateHomeProgramRequest {
        private Long childId;
        private Long activityId;
        private String title;
        private String description;
        private String priority;
        private String scheduleType;
        private String preferredTime;
        private Integer frequencyPerWeek;
        private Integer targetDurationMinutes;
        private Integer targetPromptLevelId;
        private String goalStatement;
        private String caregiverInstructions;
        private String stepSpecificInstructions;
        private String safetyConsiderations;
        private String thingsToAvoid;
        private String encouragementSuggestions;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<Long> targetStepIds;

        public CreateHomeProgramRequest() {}

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getScheduleType() { return scheduleType; }
        public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

        public String getPreferredTime() { return preferredTime; }
        public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

        public Integer getFrequencyPerWeek() { return frequencyPerWeek; }
        public void setFrequencyPerWeek(Integer frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; }

        public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
        public void setTargetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; }

        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public void setTargetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; }

        public String getGoalStatement() { return goalStatement; }
        public void setGoalStatement(String goalStatement) { this.goalStatement = goalStatement; }

        public String getCaregiverInstructions() { return caregiverInstructions; }
        public void setCaregiverInstructions(String caregiverInstructions) { this.caregiverInstructions = caregiverInstructions; }

        public String getStepSpecificInstructions() { return stepSpecificInstructions; }
        public void setStepSpecificInstructions(String stepSpecificInstructions) { this.stepSpecificInstructions = stepSpecificInstructions; }

        public String getSafetyConsiderations() { return safetyConsiderations; }
        public void setSafetyConsiderations(String safetyConsiderations) { this.safetyConsiderations = safetyConsiderations; }

        public String getThingsToAvoid() { return thingsToAvoid; }
        public void setThingsToAvoid(String thingsToAvoid) { this.thingsToAvoid = thingsToAvoid; }

        public String getEncouragementSuggestions() { return encouragementSuggestions; }
        public void setEncouragementSuggestions(String encouragementSuggestions) { this.encouragementSuggestions = encouragementSuggestions; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public List<Long> getTargetStepIds() { return targetStepIds; }
        public void setTargetStepIds(List<Long> targetStepIds) { this.targetStepIds = targetStepIds; }
    }

    public static class HomeProgramResponse {
        private Long id;
        private Long childId;
        private String childName;
        private Long therapistId;
        private String therapistName;
        private Long activityId;
        private String activityTitle;
        private String title;
        private String description;
        private String status;
        private String priority;
        private String scheduleType;
        private String preferredTime;
        private Integer frequencyPerWeek;
        private Integer targetDurationMinutes;
        private Integer targetPromptLevelId;
        private String targetPromptLevelName;
        private String goalStatement;
        private String caregiverInstructions;
        private String stepSpecificInstructions;
        private String safetyConsiderations;
        private String thingsToAvoid;
        private String encouragementSuggestions;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isActive;
        private List<ActivityDtos.TaskStepResponse> targetSteps;
        private LocalDateTime createdAt;

        public HomeProgramResponse() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long childId;
            private String childName;
            private Long therapistId;
            private String therapistName;
            private Long activityId;
            private String activityTitle;
            private String title;
            private String description;
            private String status;
            private String priority;
            private String scheduleType;
            private String preferredTime;
            private Integer frequencyPerWeek;
            private Integer targetDurationMinutes;
            private Integer targetPromptLevelId;
            private String targetPromptLevelName;
            private String goalStatement;
            private String caregiverInstructions;
            private String stepSpecificInstructions;
            private String safetyConsiderations;
            private String thingsToAvoid;
            private String encouragementSuggestions;
            private LocalDate startDate;
            private LocalDate endDate;
            private Boolean isActive;
            private List<ActivityDtos.TaskStepResponse> targetSteps;
            private LocalDateTime createdAt;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder therapistId(Long therapistId) { this.therapistId = therapistId; return this; }
            public Builder therapistName(String therapistName) { this.therapistName = therapistName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder title(String title) { this.title = title; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder priority(String priority) { this.priority = priority; return this; }
            public Builder scheduleType(String scheduleType) { this.scheduleType = scheduleType; return this; }
            public Builder preferredTime(String preferredTime) { this.preferredTime = preferredTime; return this; }
            public Builder frequencyPerWeek(Integer frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; return this; }
            public Builder targetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; return this; }
            public Builder targetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; return this; }
            public Builder targetPromptLevelName(String targetPromptLevelName) { this.targetPromptLevelName = targetPromptLevelName; return this; }
            public Builder goalStatement(String goalStatement) { this.goalStatement = goalStatement; return this; }
            public Builder caregiverInstructions(String caregiverInstructions) { this.caregiverInstructions = caregiverInstructions; return this; }
            public Builder stepSpecificInstructions(String stepSpecificInstructions) { this.stepSpecificInstructions = stepSpecificInstructions; return this; }
            public Builder safetyConsiderations(String safetyConsiderations) { this.safetyConsiderations = safetyConsiderations; return this; }
            public Builder thingsToAvoid(String thingsToAvoid) { this.thingsToAvoid = thingsToAvoid; return this; }
            public Builder encouragementSuggestions(String encouragementSuggestions) { this.encouragementSuggestions = encouragementSuggestions; return this; }
            public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
            public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
            public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
            public Builder targetSteps(List<ActivityDtos.TaskStepResponse> targetSteps) { this.targetSteps = targetSteps; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

            public HomeProgramResponse build() {
                HomeProgramResponse res = new HomeProgramResponse();
                res.id = id;
                res.childId = childId;
                res.childName = childName;
                res.therapistId = therapistId;
                res.therapistName = therapistName;
                res.activityId = activityId;
                res.activityTitle = activityTitle;
                res.title = title;
                res.description = description;
                res.status = status;
                res.priority = priority;
                res.scheduleType = scheduleType;
                res.preferredTime = preferredTime;
                res.frequencyPerWeek = frequencyPerWeek;
                res.targetDurationMinutes = targetDurationMinutes;
                res.targetPromptLevelId = targetPromptLevelId;
                res.targetPromptLevelName = targetPromptLevelName;
                res.goalStatement = goalStatement;
                res.caregiverInstructions = caregiverInstructions;
                res.stepSpecificInstructions = stepSpecificInstructions;
                res.safetyConsiderations = safetyConsiderations;
                res.thingsToAvoid = thingsToAvoid;
                res.encouragementSuggestions = encouragementSuggestions;
                res.startDate = startDate;
                res.endDate = endDate;
                res.isActive = isActive;
                res.targetSteps = targetSteps;
                res.createdAt = createdAt;
                return res;
            }
        }

        public Long getId() { return id; }
        public Long getChildId() { return childId; }
        public String getChildName() { return childName; }
        public Long getTherapistId() { return therapistId; }
        public String getTherapistName() { return therapistName; }
        public Long getActivityId() { return activityId; }
        public String getActivityTitle() { return activityTitle; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public String getScheduleType() { return scheduleType; }
        public String getPreferredTime() { return preferredTime; }
        public Integer getFrequencyPerWeek() { return frequencyPerWeek; }
        public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public String getTargetPromptLevelName() { return targetPromptLevelName; }
        public String getGoalStatement() { return goalStatement; }
        public String getCaregiverInstructions() { return caregiverInstructions; }
        public String getStepSpecificInstructions() { return stepSpecificInstructions; }
        public String getSafetyConsiderations() { return safetyConsiderations; }
        public String getThingsToAvoid() { return thingsToAvoid; }
        public String getEncouragementSuggestions() { return encouragementSuggestions; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Boolean getIsActive() { return isActive; }
        public List<ActivityDtos.TaskStepResponse> getTargetSteps() { return targetSteps; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
