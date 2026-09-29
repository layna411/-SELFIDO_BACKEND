package com.simats.selfora.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HomeProgramDtos {

    public static class CreateHomeProgramRequest {
        private Long childId;
        private Long activityId;
        private Integer frequencyPerWeek;
        private Integer targetDurationMinutes;
        private Integer targetPromptLevelId;
        private String goalStatement;
        private String caregiverInstructions;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<Long> targetStepIds;

        public CreateHomeProgramRequest() {}

        public CreateHomeProgramRequest(Long childId, Long activityId, Integer frequencyPerWeek, Integer targetDurationMinutes, Integer targetPromptLevelId, String goalStatement, String caregiverInstructions, LocalDate startDate, LocalDate endDate, List<Long> targetStepIds) {
            this.childId = childId;
            this.activityId = activityId;
            this.frequencyPerWeek = frequencyPerWeek;
            this.targetDurationMinutes = targetDurationMinutes;
            this.targetPromptLevelId = targetPromptLevelId;
            this.goalStatement = goalStatement;
            this.caregiverInstructions = caregiverInstructions;
            this.startDate = startDate;
            this.endDate = endDate;
            this.targetStepIds = targetStepIds;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
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
        private Integer frequencyPerWeek;
        private Integer targetDurationMinutes;
        private Integer targetPromptLevelId;
        private String targetPromptLevelName;
        private String goalStatement;
        private String caregiverInstructions;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isActive;
        private List<ActivityDtos.TaskStepResponse> targetSteps;
        private LocalDateTime createdAt;

        public HomeProgramResponse() {}

        public HomeProgramResponse(Long id, Long childId, String childName, Long therapistId, String therapistName, Long activityId, String activityTitle, Integer frequencyPerWeek, Integer targetDurationMinutes, Integer targetPromptLevelId, String targetPromptLevelName, String goalStatement, String caregiverInstructions, LocalDate startDate, LocalDate endDate, Boolean isActive, List<ActivityDtos.TaskStepResponse> targetSteps, LocalDateTime createdAt) {
            this.id = id;
            this.childId = childId;
            this.childName = childName;
            this.therapistId = therapistId;
            this.therapistName = therapistName;
            this.activityId = activityId;
            this.activityTitle = activityTitle;
            this.frequencyPerWeek = frequencyPerWeek;
            this.targetDurationMinutes = targetDurationMinutes;
            this.targetPromptLevelId = targetPromptLevelId;
            this.targetPromptLevelName = targetPromptLevelName;
            this.goalStatement = goalStatement;
            this.caregiverInstructions = caregiverInstructions;
            this.startDate = startDate;
            this.endDate = endDate;
            this.isActive = isActive;
            this.targetSteps = targetSteps;
            this.createdAt = createdAt;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long childId;
            private String childName;
            private Long therapistId;
            private String therapistName;
            private Long activityId;
            private String activityTitle;
            private Integer frequencyPerWeek;
            private Integer targetDurationMinutes;
            private Integer targetPromptLevelId;
            private String targetPromptLevelName;
            private String goalStatement;
            private String caregiverInstructions;
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
            public Builder frequencyPerWeek(Integer frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; return this; }
            public Builder targetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; return this; }
            public Builder targetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; return this; }
            public Builder targetPromptLevelName(String targetPromptLevelName) { this.targetPromptLevelName = targetPromptLevelName; return this; }
            public Builder goalStatement(String goalStatement) { this.goalStatement = goalStatement; return this; }
            public Builder caregiverInstructions(String caregiverInstructions) { this.caregiverInstructions = caregiverInstructions; return this; }
            public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
            public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
            public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
            public Builder targetSteps(List<ActivityDtos.TaskStepResponse> targetSteps) { this.targetSteps = targetSteps; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
            public HomeProgramResponse build() { return new HomeProgramResponse(id, childId, childName, therapistId, therapistName, activityId, activityTitle, frequencyPerWeek, targetDurationMinutes, targetPromptLevelId, targetPromptLevelName, goalStatement, caregiverInstructions, startDate, endDate, isActive, targetSteps, createdAt); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
        public Long getTherapistId() { return therapistId; }
        public void setTherapistId(Long therapistId) { this.therapistId = therapistId; }
        public String getTherapistName() { return therapistName; }
        public void setTherapistName(String therapistName) { this.therapistName = therapistName; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getActivityTitle() { return activityTitle; }
        public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
        public Integer getFrequencyPerWeek() { return frequencyPerWeek; }
        public void setFrequencyPerWeek(Integer frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; }
        public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
        public void setTargetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; }
        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public void setTargetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; }
        public String getTargetPromptLevelName() { return targetPromptLevelName; }
        public void setTargetPromptLevelName(String targetPromptLevelName) { this.targetPromptLevelName = targetPromptLevelName; }
        public String getGoalStatement() { return goalStatement; }
        public void setGoalStatement(String goalStatement) { this.goalStatement = goalStatement; }
        public String getCaregiverInstructions() { return caregiverInstructions; }
        public void setCaregiverInstructions(String caregiverInstructions) { this.caregiverInstructions = caregiverInstructions; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public List<ActivityDtos.TaskStepResponse> getTargetSteps() { return targetSteps; }
        public void setTargetSteps(List<ActivityDtos.TaskStepResponse> targetSteps) { this.targetSteps = targetSteps; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
