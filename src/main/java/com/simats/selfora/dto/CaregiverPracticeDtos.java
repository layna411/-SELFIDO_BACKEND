package com.simats.selfora.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CaregiverPracticeDtos {

    public static class StepResultItemDto {
        private Long stepId;
        private Integer stepNumber;
        private String stepTitle;
        private String outcome; // INDEPENDENT, LITTLE_HELP, LOT_HELP, COULD_NOT_COMPLETE, NOT_PRACTISED
        private String observation;

        public StepResultItemDto() {}

        public StepResultItemDto(Long stepId, Integer stepNumber, String stepTitle, String outcome, String observation) {
            this.stepId = stepId;
            this.stepNumber = stepNumber;
            this.stepTitle = stepTitle;
            this.outcome = outcome;
            this.observation = observation;
        }

        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }

        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

        public String getStepTitle() { return stepTitle; }
        public void setStepTitle(String stepTitle) { this.stepTitle = stepTitle; }

        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }

        public String getObservation() { return observation; }
        public void setObservation(String observation) { this.observation = observation; }
    }

    public static class SubmitPracticeRequest {
        private Long homeProgramId;
        private Long childId;
        private Long activityId;
        private LocalDate practiceDate;
        private Integer durationMinutes;
        private String caregiverNotes;
        private List<StepResultItemDto> stepResults;

        public SubmitPracticeRequest() {}

        public Long getHomeProgramId() { return homeProgramId; }
        public void setHomeProgramId(Long homeProgramId) { this.homeProgramId = homeProgramId; }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public LocalDate getPracticeDate() { return practiceDate; }
        public void setPracticeDate(LocalDate practiceDate) { this.practiceDate = practiceDate; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getCaregiverNotes() { return caregiverNotes; }
        public void setCaregiverNotes(String caregiverNotes) { this.caregiverNotes = caregiverNotes; }

        public List<StepResultItemDto> getStepResults() { return stepResults; }
        public void setStepResults(List<StepResultItemDto> stepResults) { this.stepResults = stepResults; }
    }

    public static class TherapistReviewRequest {
        private String feedbackText;
        private String status; // REVIEWED, FLAGGED
        private Boolean flaggedForFollowup;

        public TherapistReviewRequest() {}

        public String getFeedbackText() { return feedbackText; }
        public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Boolean getFlaggedForFollowup() { return flaggedForFollowup; }
        public void setFlaggedForFollowup(Boolean flaggedForFollowup) { this.flaggedForFollowup = flaggedForFollowup; }
    }

    public static class CaregiverPracticeSessionResponse {
        private Long id;
        private Long homeProgramId;
        private String homeProgramTitle;
        private Long childId;
        private String childName;
        private Long caregiverId;
        private String caregiverName;
        private Long activityId;
        private String activityTitle;
        private LocalDate practiceDate;
        private Integer durationMinutes;
        private String caregiverNotes;
        private String status;
        private LocalDateTime submittedAt;
        private String reviewingTherapistName;
        private LocalDateTime reviewedAt;
        private String therapistFeedback;
        private Boolean flaggedForFollowup;
        private List<StepResultItemDto> stepResults;

        public CaregiverPracticeSessionResponse() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long homeProgramId;
            private String homeProgramTitle;
            private Long childId;
            private String childName;
            private Long caregiverId;
            private String caregiverName;
            private Long activityId;
            private String activityTitle;
            private LocalDate practiceDate;
            private Integer durationMinutes;
            private String caregiverNotes;
            private String status;
            private LocalDateTime submittedAt;
            private String reviewingTherapistName;
            private LocalDateTime reviewedAt;
            private String therapistFeedback;
            private Boolean flaggedForFollowup;
            private List<StepResultItemDto> stepResults;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder homeProgramId(Long homeProgramId) { this.homeProgramId = homeProgramId; return this; }
            public Builder homeProgramTitle(String homeProgramTitle) { this.homeProgramTitle = homeProgramTitle; return this; }
            public Builder childId(Long childId) { this.childId = childId; return this; }
            public Builder childName(String childName) { this.childName = childName; return this; }
            public Builder caregiverId(Long caregiverId) { this.caregiverId = caregiverId; return this; }
            public Builder caregiverName(String caregiverName) { this.caregiverName = caregiverName; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder activityTitle(String activityTitle) { this.activityTitle = activityTitle; return this; }
            public Builder practiceDate(LocalDate practiceDate) { this.practiceDate = practiceDate; return this; }
            public Builder durationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }
            public Builder caregiverNotes(String caregiverNotes) { this.caregiverNotes = caregiverNotes; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
            public Builder reviewingTherapistName(String reviewingTherapistName) { this.reviewingTherapistName = reviewingTherapistName; return this; }
            public Builder reviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; return this; }
            public Builder therapistFeedback(String therapistFeedback) { this.therapistFeedback = therapistFeedback; return this; }
            public Builder flaggedForFollowup(Boolean flaggedForFollowup) { this.flaggedForFollowup = flaggedForFollowup; return this; }
            public Builder stepResults(List<StepResultItemDto> stepResults) { this.stepResults = stepResults; return this; }

            public CaregiverPracticeSessionResponse build() {
                CaregiverPracticeSessionResponse res = new CaregiverPracticeSessionResponse();
                res.id = id;
                res.homeProgramId = homeProgramId;
                res.homeProgramTitle = homeProgramTitle;
                res.childId = childId;
                res.childName = childName;
                res.caregiverId = caregiverId;
                res.caregiverName = caregiverName;
                res.activityId = activityId;
                res.activityTitle = activityTitle;
                res.practiceDate = practiceDate;
                res.durationMinutes = durationMinutes;
                res.caregiverNotes = caregiverNotes;
                res.status = status;
                res.submittedAt = submittedAt;
                res.reviewingTherapistName = reviewingTherapistName;
                res.reviewedAt = reviewedAt;
                res.therapistFeedback = therapistFeedback;
                res.flaggedForFollowup = flaggedForFollowup;
                res.stepResults = stepResults;
                return res;
            }
        }

        public Long getId() { return id; }
        public Long getHomeProgramId() { return homeProgramId; }
        public String getHomeProgramTitle() { return homeProgramTitle; }
        public Long getChildId() { return childId; }
        public String getChildName() { return childName; }
        public Long getCaregiverId() { return caregiverId; }
        public String getCaregiverName() { return caregiverName; }
        public Long getActivityId() { return activityId; }
        public String getActivityTitle() { return activityTitle; }
        public LocalDate getPracticeDate() { return practiceDate; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public String getCaregiverNotes() { return caregiverNotes; }
        public String getStatus() { return status; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public String getReviewingTherapistName() { return reviewingTherapistName; }
        public LocalDateTime getReviewedAt() { return reviewedAt; }
        public String getTherapistFeedback() { return therapistFeedback; }
        public Boolean getFlaggedForFollowup() { return flaggedForFollowup; }
        public List<StepResultItemDto> getStepResults() { return stepResults; }
    }
}
