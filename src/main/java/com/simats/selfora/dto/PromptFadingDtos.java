package com.simats.selfora.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PromptFadingDtos {

    public static class CreatePromptFadingPlanRequest {
        private Long childId;
        private Long activityId;
        private Long stepId;
        private Integer currentPromptLevelId;
        private Integer targetPromptLevelId;
        private String clinicalReason;
        private String reviewCriteria;
        private String notes;

        public CreatePromptFadingPlanRequest() {}

        public CreatePromptFadingPlanRequest(Long childId, Long activityId, Long stepId, Integer currentPromptLevelId, Integer targetPromptLevelId, String clinicalReason, String reviewCriteria, String notes) {
            this.childId = childId;
            this.activityId = activityId;
            this.stepId = stepId;
            this.currentPromptLevelId = currentPromptLevelId;
            this.targetPromptLevelId = targetPromptLevelId;
            this.clinicalReason = clinicalReason;
            this.reviewCriteria = reviewCriteria;
            this.notes = notes;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }

        public Integer getCurrentPromptLevelId() { return currentPromptLevelId; }
        public void setCurrentPromptLevelId(Integer currentPromptLevelId) { this.currentPromptLevelId = currentPromptLevelId; }

        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public void setTargetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; }

        public String getClinicalReason() { return clinicalReason; }
        public void setClinicalReason(String clinicalReason) { this.clinicalReason = clinicalReason; }

        public String getReviewCriteria() { return reviewCriteria; }
        public void setReviewCriteria(String reviewCriteria) { this.reviewCriteria = reviewCriteria; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class PromptFadingPlanResponse {
        private Long id;
        private Long childId;
        private String childName;
        private Long activityId;
        private String activityTitle;
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private Integer currentPromptLevelId;
        private String currentPromptLevelName;
        private Integer targetPromptLevelId;
        private String targetPromptLevelName;
        private String clinicalReason;
        private String reviewCriteria;
        private String notes;
        private String status;
        private String approvedByTherapistName;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;

        public PromptFadingPlanResponse() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long childId;
            private String childName;
            private Long activityId;
            private String activityTitle;
            private Long stepId;
            private Integer stepNumber;
            private String stepTitle;
            private Integer currentPromptLevelId;
            private String currentPromptLevelName;
            private Integer targetPromptLevelId;
            private String targetPromptLevelName;
            private String clinicalReason;
            private String reviewCriteria;
            private String notes;
            private String status;
            private String approvedByTherapistName;
            private LocalDateTime approvedAt;
            private LocalDateTime createdAt;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder stepId(Long stepId) { this.stepId = stepId; return this; }
            public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
            public Builder stepTitle(String stepTitle) { this.stepTitle = stepTitle; return this; }
            public Builder currentPromptLevelId(Integer currentPromptLevelId) { this.currentPromptLevelId = currentPromptLevelId; return this; }
            public Builder currentPromptLevelName(String currentPromptLevelName) { this.currentPromptLevelName = currentPromptLevelName; return this; }
            public Builder targetPromptLevelId(Integer targetPromptLevelId) { this.targetPromptLevelId = targetPromptLevelId; return this; }
            public Builder targetPromptLevelName(String targetPromptLevelName) { this.targetPromptLevelName = targetPromptLevelName; return this; }
            public Builder clinicalReason(String clinicalReason) { this.clinicalReason = clinicalReason; return this; }
            public Builder reviewCriteria(String reviewCriteria) { this.reviewCriteria = reviewCriteria; return this; }
            public Builder notes(String notes) { this.notes = notes; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder approvedByTherapistName(String approvedByTherapistName) { this.approvedByTherapistName = approvedByTherapistName; return this; }
            public Builder approvedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

            public PromptFadingPlanResponse build() {
                PromptFadingPlanResponse res = new PromptFadingPlanResponse();
                res.id = id;
                res.childId = childId;
                res.childName = childName;
                res.activityId = activityId;
                res.activityTitle = activityTitle;
                res.stepId = stepId;
                res.stepNumber = stepNumber;
                res.stepTitle = stepTitle;
                res.currentPromptLevelId = currentPromptLevelId;
                res.currentPromptLevelName = currentPromptLevelName;
                res.targetPromptLevelId = targetPromptLevelId;
                res.targetPromptLevelName = targetPromptLevelName;
                res.clinicalReason = clinicalReason;
                res.reviewCriteria = reviewCriteria;
                res.notes = notes;
                res.status = status;
                res.approvedByTherapistName = approvedByTherapistName;
                res.approvedAt = approvedAt;
                res.createdAt = createdAt;
                return res;
            }
        }

        public Long getId() { return id; }
        public Long getChildId() { return childId; }
        public String getChildName() { return childName; }
        public Long getActivityId() { return activityId; }
        public String getActivityTitle() { return activityTitle; }
        public Long getStepId() { return stepId; }
        public Integer getStepNumber() { return stepNumber; }
        public String getStepTitle() { return stepTitle; }
        public Integer getCurrentPromptLevelId() { return currentPromptLevelId; }
        public String getCurrentPromptLevelName() { return currentPromptLevelName; }
        public Integer getTargetPromptLevelId() { return targetPromptLevelId; }
        public String getTargetPromptLevelName() { return targetPromptLevelName; }
        public String getClinicalReason() { return clinicalReason; }
        public String getReviewCriteria() { return reviewCriteria; }
        public String getNotes() { return notes; }
        public String getStatus() { return status; }
        public String getApprovedByTherapistName() { return approvedByTherapistName; }
        public LocalDateTime getApprovedAt() { return approvedAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class PromptFadingHistoryItemResponse {
        private Long id;
        private Long childId;
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private String approvedByTherapistName;
        private String previousPromptLevelName;
        private String newPromptLevelName;
        private LocalDateTime changeDate;
        private String reason;
        private String subsequentPerformanceNotes;

        public PromptFadingHistoryItemResponse() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long childId;
            private Long stepId;
            private Integer stepNumber;
            private String stepTitle;
            private String approvedByTherapistName;
            private String previousPromptLevelName;
            private String newPromptLevelName;
            private LocalDateTime changeDate;
            private String reason;
            private String subsequentPerformanceNotes;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder stepId(Long stepId) { this.stepId = stepId; return this; }
            public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
            public Builder stepTitle(String stepTitle) { this.stepTitle = stepTitle; return this; }
            public Builder approvedByTherapistName(String approvedByTherapistName) { this.approvedByTherapistName = approvedByTherapistName; return this; }
            public Builder previousPromptLevelName(String previousPromptLevelName) { this.previousPromptLevelName = previousPromptLevelName; return this; }
            public Builder newPromptLevelName(String newPromptLevelName) { this.newPromptLevelName = newPromptLevelName; return this; }
            public Builder changeDate(LocalDateTime changeDate) { this.changeDate = changeDate; return this; }
            public Builder reason(String reason) { this.reason = reason; return this; }
            public Builder subsequentPerformanceNotes(String subsequentPerformanceNotes) { this.subsequentPerformanceNotes = subsequentPerformanceNotes; return this; }

            public PromptFadingHistoryItemResponse build() {
                PromptFadingHistoryItemResponse res = new PromptFadingHistoryItemResponse();
                res.id = id;
                res.childId = childId;
                res.stepId = stepId;
                res.stepNumber = stepNumber;
                res.stepTitle = stepTitle;
                res.approvedByTherapistName = approvedByTherapistName;
                res.previousPromptLevelName = previousPromptLevelName;
                res.newPromptLevelName = newPromptLevelName;
                res.changeDate = changeDate;
                res.reason = reason;
                res.subsequentPerformanceNotes = subsequentPerformanceNotes;
                return res;
            }
        }

        public Long getId() { return id; }
        public Long getChildId() { return childId; }
        public Long getStepId() { return stepId; }
        public Integer getStepNumber() { return stepNumber; }
        public String getStepTitle() { return stepTitle; }
        public String getApprovedByTherapistName() { return approvedByTherapistName; }
        public String getPreviousPromptLevelName() { return previousPromptLevelName; }
        public String getNewPromptLevelName() { return newPromptLevelName; }
        public LocalDateTime getChangeDate() { return changeDate; }
        public String getReason() { return reason; }
        public String getSubsequentPerformanceNotes() { return subsequentPerformanceNotes; }
    }
}
