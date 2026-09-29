package com.simats.selfora.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdaptivePlanDtos {

    public static class CreateAdaptivePlanRequest {
        private Long childId;
        private Long activityId;
        private Long sessionId;
        private Integer currentPromptLevelId;
        private Integer targetPromptLevelId;
        private List<Long> targetStepIds;
        private String clinicalRationale;
        private Boolean isConfirmed;

        public CreateAdaptivePlanRequest() {}

        public CreateAdaptivePlanRequest(Long childId, Long activityId, Long sessionId, Integer currentPromptLevelId, Integer targetPromptLevelId, List<Long> targetStepIds, String clinicalRationale, Boolean isConfirmed) {
            this.childId = childId;
            this.activityId = activityId;
            this.sessionId = sessionId;
            this.currentPromptLevelId = currentPromptLevelId;
            this.targetPromptLevelId = targetPromptLevelId;
            this.targetStepIds = targetStepIds;
            this.clinicalRationale = clinicalRationale;
            this.isConfirmed = isConfirmed;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Integer getCurrentPromptLevelId() { return currentPromptLevelId; }
        public void setCurrentPromptLevelId(Integer currentPromptLevelId) { this.currentPromptLevelId = currentPromptLevelId; }
        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public void setTargetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; }
        public List<Long> getTargetStepIds() { return targetStepIds; }
        public void setTargetStepIds(List<Long> targetStepIds) { this.targetStepIds = targetStepIds; }
        public String getClinicalRationale() { return clinicalRationale; }
        public void setClinicalRationale(String clinicalRationale) { this.clinicalRationale = clinicalRationale; }
        public Boolean getIsConfirmed() { return isConfirmed; }
        public void setIsConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; }
    }

    public static class AdaptivePlanResponse {
        private Long id;
        private Long childId;
        private String childName;
        private Long activityId;
        private String activityTitle;
        private Long sessionId;
        private Integer currentPromptLevelId;
        private String currentPromptLevelName;
        private Integer targetPromptLevelId;
        private String targetPromptLevelName;
        private List<Long> targetStepIds;
        private String clinicalRationale;
        private Boolean isConfirmed;
        private LocalDateTime createdAt;

        public AdaptivePlanResponse() {}

        public AdaptivePlanResponse(Long id, Long childId, String childName, Long activityId, String activityTitle, Long sessionId, Integer currentPromptLevelId, String currentPromptLevelName, Integer targetPromptLevelId, String targetPromptLevelName, List<Long> targetStepIds, String clinicalRationale, Boolean isConfirmed, LocalDateTime createdAt) {
            this.id = id;
            this.childId = childId;
            this.childName = childName;
            this.activityId = activityId;
            this.activityTitle = activityTitle;
            this.sessionId = sessionId;
            this.currentPromptLevelId = currentPromptLevelId;
            this.currentPromptLevelName = currentPromptLevelName;
            this.targetPromptLevelId = targetPromptLevelId;
            this.targetPromptLevelName = targetPromptLevelName;
            this.targetStepIds = targetStepIds;
            this.clinicalRationale = clinicalRationale;
            this.isConfirmed = isConfirmed;
            this.createdAt = createdAt;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long childId;
            private String childName;
            private Long activityId;
            private String activityTitle;
            private Long sessionId;
            private Integer currentPromptLevelId;
            private String currentPromptLevelName;
            private Integer targetPromptLevelId;
            private String targetPromptLevelName;
            private List<Long> targetStepIds;
            private String clinicalRationale;
            private Boolean isConfirmed;
            private LocalDateTime createdAt;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
            public Builder currentPromptLevelId(Integer currentPromptLevelId) { this.currentPromptLevelId = currentPromptLevelId; return this; }
            public Builder currentPromptLevelName(String currentPromptLevelName) { this.currentPromptLevelName = currentPromptLevelName; return this; }
            public Builder targetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; return this; }
            public Builder targetPromptLevelName(String targetPromptLevelName) { this.targetPromptLevelName = targetPromptLevelName; return this; }
            public Builder targetStepIds(List<Long> targetStepIds) { this.targetStepIds = targetStepIds; return this; }
            public Builder clinicalRationale(String clinicalRationale) { this.clinicalRationale = clinicalRationale; return this; }
            public Builder isConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
            public AdaptivePlanResponse build() { return new AdaptivePlanResponse(id, childId, childName, activityId, activityTitle, sessionId, currentPromptLevelId, currentPromptLevelName, targetPromptLevelId, targetPromptLevelName, targetStepIds, clinicalRationale, isConfirmed, createdAt); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getActivityTitle() { return activityTitle; }
        public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Integer getCurrentPromptLevelId() { return currentPromptLevelId; }
        public void setCurrentPromptLevelId(Integer currentPromptLevelId) { this.currentPromptLevelId = currentPromptLevelId; }
        public String getCurrentPromptLevelName() { return currentPromptLevelName; }
        public void setCurrentPromptLevelName(String currentPromptLevelName) { this.currentPromptLevelName = currentPromptLevelName; }
        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public void setTargetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; }
        public String getTargetPromptLevelName() { return targetPromptLevelName; }
        public void setTargetPromptLevelName(String targetPromptLevelName) { this.targetPromptLevelName = targetPromptLevelName; }
        public List<Long> getTargetStepIds() { return targetStepIds; }
        public void setTargetStepIds(List<Long> targetStepIds) { this.targetStepIds = targetStepIds; }
        public String getClinicalRationale() { return clinicalRationale; }
        public void setClinicalRationale(String clinicalRationale) { this.clinicalRationale = clinicalRationale; }
        public Boolean getIsConfirmed() { return isConfirmed; }
        public void setIsConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
