package com.simats.selfora.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AssessmentDtos {

    public static class StepAssessmentRequest {
        private Long stepId;
        private Integer promptLevelId;
        private String outcome;
        private String notes;

        public StepAssessmentRequest() {}

        public StepAssessmentRequest(Long stepId, Integer promptLevelId, String outcome, String notes) {
            this.stepId = stepId;
            this.promptLevelId = promptLevelId;
            this.outcome = outcome;
            this.notes = notes;
        }

        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }
        public Integer getPromptLevelId() { return promptLevelId; }
        public void setPromptLevelId(Integer promptLevelId) { this.promptLevelId = promptLevelId; }
        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class CreateAssessmentRequest {
        private Long childId;
        private Long activityId;
        private String notes;
        private List<StepAssessmentRequest> stepResults;

        public CreateAssessmentRequest() {}

        public CreateAssessmentRequest(Long childId, Long activityId, String notes, List<StepAssessmentRequest> stepResults) {
            this.childId = childId;
            this.activityId = activityId;
            this.notes = notes;
            this.stepResults = stepResults;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public List<StepAssessmentRequest> getStepResults() { return stepResults; }
        public void setStepResults(List<StepAssessmentRequest> stepResults) { this.stepResults = stepResults; }
    }

    public static class AssessmentResponse {
        private Long assessmentId;
        private Long childId;
        private String childName;
        private Long therapistId;
        private String therapistName;
        private Long activityId;
        private String activityTitle;
        private LocalDateTime assessmentDate;
        private String notes;
        private List<StepResultResponse> stepResults;

        public AssessmentResponse() {}

        public AssessmentResponse(Long assessmentId, Long childId, String childName, Long therapistId, String therapistName, Long activityId, String activityTitle, LocalDateTime assessmentDate, String notes, List<StepResultResponse> stepResults) {
            this.assessmentId = assessmentId;
            this.childId = childId;
            this.childName = childName;
            this.therapistId = therapistId;
            this.therapistName = therapistName;
            this.activityId = activityId;
            this.activityTitle = activityTitle;
            this.assessmentDate = assessmentDate;
            this.notes = notes;
            this.stepResults = stepResults;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long assessmentId;
            private Long childId;
            private String childName;
            private Long therapistId;
            private String therapistName;
            private Long activityId;
            private String activityTitle;
            private LocalDateTime assessmentDate;
            private String notes;
            private List<StepResultResponse> stepResults;

            public Builder assessmentId(Long assessmentId) { this.assessmentId = assessmentId; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder therapistId(Long therapistId) { this.therapistId = therapistId; return this; }
            public Builder therapistName(String therapistName) { this.therapistName = therapistName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder assessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; return this; }
            public Builder notes(String notes) { this.notes = notes; return this; }
            public Builder stepResults(List<StepResultResponse> stepResults) { this.stepResults = stepResults; return this; }
            public AssessmentResponse build() { return new AssessmentResponse(assessmentId, childId, childName, therapistId, therapistName, activityId, activityTitle, assessmentDate, notes, stepResults); }
        }

        public Long getAssessmentId() { return assessmentId; }
        public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
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
        public LocalDateTime getAssessmentDate() { return assessmentDate; }
        public void setAssessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public List<StepResultResponse> getStepResults() { return stepResults; }
        public void setStepResults(List<StepResultResponse> stepResults) { this.stepResults = stepResults; }
    }

    public static class StepResultResponse {
        private Long id;
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private Integer promptLevelId;
        private String promptLevelName;
        private String outcome;
        private String notes;

        public StepResultResponse() {}

        public StepResultResponse(Long id, Long stepId, Integer stepNumber, String stepTitle, Integer promptLevelId, String promptLevelName, String outcome, String notes) {
            this.id = id;
            this.stepId = stepId;
            this.stepNumber = stepNumber;
            this.stepTitle = stepTitle;
            this.promptLevelId = promptLevelId;
            this.promptLevelName = promptLevelName;
            this.outcome = outcome;
            this.notes = notes;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long stepId;
            private Integer stepNumber;
            private String stepTitle;
            private Integer promptLevelId;
            private String promptLevelName;
            private String outcome;
            private String notes;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder stepId(Long stepId) { this.stepId = stepId; return this; }
            public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
            public Builder stepTitle(String stepTitle) { this.stepTitle = stepTitle; return this; }
            public Builder promptLevelId(Integer promptLevelId) { this.promptLevelId = promptLevelId; return this; }
            public Builder promptLevelName(String promptLevelName) { this.promptLevelName = promptLevelName; return this; }
            public Builder outcome(String outcome) { this.outcome = outcome; return this; }
            public Builder notes(String notes) { this.notes = notes; return this; }
            public StepResultResponse build() { return new StepResultResponse(id, stepId, stepNumber, stepTitle, promptLevelId, promptLevelName, outcome, notes); }
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
        public String getPromptLevelName() { return promptLevelName; }
        public void setPromptLevelName(String promptLevelName) { this.promptLevelName = promptLevelName; }
        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
