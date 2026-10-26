package com.simats.selfora.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TherapistDtos {

    public static class CreateChildWithCaregiverRequest {
        // Step 1: Child Info
        private String childFirstName;
        private String childLastName;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateOfBirth;
        private String gender;
        private String profilePhotoUrl;
        private String registrationId;
        private String diagnosisNotes;

        // Step 2: Caregiver Info
        private String caregiverName;
        private String relationship; // Father, Mother, Guardian, Other
        private String phone;
        private String email;
        private String address;

        public CreateChildWithCaregiverRequest() {}

        public String getChildFirstName() { return childFirstName; }
        public void setChildFirstName(String childFirstName) { this.childFirstName = childFirstName; }

        public String getChildLastName() { return childLastName; }
        public void setChildLastName(String childLastName) { this.childLastName = childLastName; }

        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getProfilePhotoUrl() { return profilePhotoUrl; }
        public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

        public String getRegistrationId() { return registrationId; }
        public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

        public String getDiagnosisNotes() { return diagnosisNotes; }
        public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }

        public String getCaregiverName() { return caregiverName; }
        public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class CredentialsSuccessResponse {
        private Long childId;
        private String childName;
        private Long caregiverId;
        private String caregiverName;
        private String relationship;
        private String caregiverEmail;
        private String temporaryPassword;
        private Boolean mustChangePassword;

        public CredentialsSuccessResponse() {}

        public CredentialsSuccessResponse(Long childId, String childName, Long caregiverId, String caregiverName, String relationship, String caregiverEmail, String temporaryPassword, Boolean mustChangePassword) {
            this.childId = childId;
            this.childName = childName;
            this.caregiverId = caregiverId;
            this.caregiverName = caregiverName;
            this.relationship = relationship;
            this.caregiverEmail = caregiverEmail;
            this.temporaryPassword = temporaryPassword;
            this.mustChangePassword = mustChangePassword;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }

        public Long getCaregiverId() { return caregiverId; }
        public void setCaregiverId(Long caregiverId) { this.caregiverId = caregiverId; }

        public String getCaregiverName() { return caregiverName; }
        public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public String getCaregiverEmail() { return caregiverEmail; }
        public void setCaregiverEmail(String caregiverEmail) { this.caregiverEmail = caregiverEmail; }

        public String getTemporaryPassword() { return temporaryPassword; }
        public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }

        public Boolean getMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    }

    public static class ScheduledSessionItem {
        private Long sessionId;
        private Long childId;
        private String childName;
        private String scheduledTime;
        private String adlActivity;
        private String status; // SCHEDULED, IN_PROGRESS, COMPLETED

        public ScheduledSessionItem() {}

        public ScheduledSessionItem(Long sessionId, Long childId, String childName, String scheduledTime, String adlActivity, String status) {
            this.sessionId = sessionId;
            this.childId = childId;
            this.childName = childName;
            this.scheduledTime = scheduledTime;
            this.adlActivity = adlActivity;
            this.status = status;
        }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }

        public String getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

        public String getAdlActivity() { return adlActivity; }
        public void setAdlActivity(String adlActivity) { this.adlActivity = adlActivity; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class PendingReviewItem {
        private Long practiceId;
        private Long childId;
        private String childName;
        private String caregiverName;
        private String activityName;
        private String submittedDate;
        private String notes;

        public PendingReviewItem() {}

        public PendingReviewItem(Long practiceId, Long childId, String childName, String caregiverName, String activityName, String submittedDate, String notes) {
            this.practiceId = practiceId;
            this.childId = childId;
            this.childName = childName;
            this.caregiverName = caregiverName;
            this.activityName = activityName;
            this.submittedDate = submittedDate;
            this.notes = notes;
        }

        public Long getPracticeId() { return practiceId; }
        public void setPracticeId(Long practiceId) { this.practiceId = practiceId; }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }

        public String getCaregiverName() { return caregiverName; }
        public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

        public String getActivityName() { return activityName; }
        public void setActivityName(String activityName) { this.activityName = activityName; }

        public String getSubmittedDate() { return submittedDate; }
        public void setSubmittedDate(String submittedDate) { this.submittedDate = submittedDate; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class TherapistDashboardSummaryResponse {
        private String therapistName;
        private String designation = "Senior Occupational Specialist";
        private String avatarUrl = "ic_therapist_default";
        private int activeChildrenCount;
        private int linkedCaregiversCount;
        private int pendingAssessmentsCount;
        private int activeHomeProgramsCount;
        private int todaySessionsCount;
        private int completedSessionsCount;
        private int pendingReviewsCount;
        private List<ScheduledSessionItem> todaySchedule = new ArrayList<>();
        private List<PendingReviewItem> pendingReviews = new ArrayList<>();

        public TherapistDashboardSummaryResponse() {}

        public TherapistDashboardSummaryResponse(String therapistName, String designation, String avatarUrl, int activeChildrenCount, int linkedCaregiversCount, int pendingAssessmentsCount, int activeHomeProgramsCount, int todaySessionsCount, int completedSessionsCount, int pendingReviewsCount, List<ScheduledSessionItem> todaySchedule, List<PendingReviewItem> pendingReviews) {
            this.therapistName = therapistName;
            this.designation = designation != null ? designation : "Senior Occupational Specialist";
            this.avatarUrl = avatarUrl != null ? avatarUrl : "ic_therapist_default";
            this.activeChildrenCount = activeChildrenCount;
            this.linkedCaregiversCount = linkedCaregiversCount;
            this.pendingAssessmentsCount = pendingAssessmentsCount;
            this.activeHomeProgramsCount = activeHomeProgramsCount;
            this.todaySessionsCount = todaySessionsCount;
            this.completedSessionsCount = completedSessionsCount;
            this.pendingReviewsCount = pendingReviewsCount;
            this.todaySchedule = todaySchedule != null ? todaySchedule : new ArrayList<>();
            this.pendingReviews = pendingReviews != null ? pendingReviews : new ArrayList<>();
        }

        public String getTherapistName() { return therapistName; }
        public void setTherapistName(String therapistName) { this.therapistName = therapistName; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public int getActiveChildrenCount() { return activeChildrenCount; }
        public void setActiveChildrenCount(int activeChildrenCount) { this.activeChildrenCount = activeChildrenCount; }

        public int getLinkedCaregiversCount() { return linkedCaregiversCount; }
        public void setLinkedCaregiversCount(int linkedCaregiversCount) { this.linkedCaregiversCount = linkedCaregiversCount; }

        public int getPendingAssessmentsCount() { return pendingAssessmentsCount; }
        public void setPendingAssessmentsCount(int pendingAssessmentsCount) { this.pendingAssessmentsCount = pendingAssessmentsCount; }

        public int getActiveHomeProgramsCount() { return activeHomeProgramsCount; }
        public void setActiveHomeProgramsCount(int activeHomeProgramsCount) { this.activeHomeProgramsCount = activeHomeProgramsCount; }

        public int getTodaySessionsCount() { return todaySessionsCount; }
        public void setTodaySessionsCount(int todaySessionsCount) { this.todaySessionsCount = todaySessionsCount; }

        public int getCompletedSessionsCount() { return completedSessionsCount; }
        public void setCompletedSessionsCount(int completedSessionsCount) { this.completedSessionsCount = completedSessionsCount; }

        public int getPendingReviewsCount() { return pendingReviewsCount; }
        public void setPendingReviewsCount(int pendingReviewsCount) { this.pendingReviewsCount = pendingReviewsCount; }

        public List<ScheduledSessionItem> getTodaySchedule() { return todaySchedule; }
        public void setTodaySchedule(List<ScheduledSessionItem> todaySchedule) { this.todaySchedule = todaySchedule; }

        public List<PendingReviewItem> getPendingReviews() { return pendingReviews; }
        public void setPendingReviews(List<PendingReviewItem> pendingReviews) { this.pendingReviews = pendingReviews; }
    }

    public static class ChildSummaryItem {
        private Long id;
        private String name;
        private int age;
        private String gender = "BOY";
        private String avatarUrl = "ic_avatar_default";
        private String caregiverName;
        private String caregiverRelationship;
        private String lastSessionDate = "No recent session";
        private String programmeStatus = "ACTIVE";
        private int assignedGoalsCount = 3;
        private int dressingPercentage;
        private int eatingPercentage;
        private int shoesPercentage;
        private int overallProgressPercentage = 75;
        private Boolean hasPendingAssessment;
        private Boolean hasActiveHomeProgram;

        public ChildSummaryItem() {}

        public ChildSummaryItem(Long id, String name, int age, String gender, String avatarUrl, String caregiverName, String caregiverRelationship, String lastSessionDate, String programmeStatus, int assignedGoalsCount, int dressingPercentage, int eatingPercentage, int shoesPercentage, int overallProgressPercentage, Boolean hasPendingAssessment, Boolean hasActiveHomeProgram) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender != null ? gender : "BOY";
            this.avatarUrl = avatarUrl != null ? avatarUrl : "ic_avatar_default";
            this.caregiverName = caregiverName;
            this.caregiverRelationship = caregiverRelationship;
            this.lastSessionDate = lastSessionDate != null ? lastSessionDate : "No recent session";
            this.programmeStatus = programmeStatus != null ? programmeStatus : "ACTIVE";
            this.assignedGoalsCount = assignedGoalsCount;
            this.dressingPercentage = dressingPercentage;
            this.eatingPercentage = eatingPercentage;
            this.shoesPercentage = shoesPercentage;
            this.overallProgressPercentage = overallProgressPercentage;
            this.hasPendingAssessment = hasPendingAssessment;
            this.hasActiveHomeProgram = hasActiveHomeProgram;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public String getCaregiverName() { return caregiverName; }
        public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

        public String getCaregiverRelationship() { return caregiverRelationship; }
        public void setCaregiverRelationship(String caregiverRelationship) { this.caregiverRelationship = caregiverRelationship; }

        public String getLastSessionDate() { return lastSessionDate; }
        public void setLastSessionDate(String lastSessionDate) { this.lastSessionDate = lastSessionDate; }

        public String getProgrammeStatus() { return programmeStatus; }
        public void setProgrammeStatus(String programmeStatus) { this.programmeStatus = programmeStatus; }

        public int getAssignedGoalsCount() { return assignedGoalsCount; }
        public void setAssignedGoalsCount(int assignedGoalsCount) { this.assignedGoalsCount = assignedGoalsCount; }

        public int getDressingPercentage() { return dressingPercentage; }
        public void setDressingPercentage(int dressingPercentage) { this.dressingPercentage = dressingPercentage; }

        public int getEatingPercentage() { return eatingPercentage; }
        public void setEatingPercentage(int eatingPercentage) { this.eatingPercentage = eatingPercentage; }

        public int getShoesPercentage() { return shoesPercentage; }
        public void setShoesPercentage(int shoesPercentage) { this.shoesPercentage = shoesPercentage; }

        public int getOverallProgressPercentage() { return overallProgressPercentage; }
        public void setOverallProgressPercentage(int overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }

        public Boolean getHasPendingAssessment() { return hasPendingAssessment; }
        public void setHasPendingAssessment(Boolean hasPendingAssessment) { this.hasPendingAssessment = hasPendingAssessment; }

        public Boolean getHasActiveHomeProgram() { return hasActiveHomeProgram; }
        public void setHasActiveHomeProgram(Boolean hasActiveHomeProgram) { this.hasActiveHomeProgram = hasActiveHomeProgram; }
    }

    public static class ResetPasswordResultResponse {
        private Long caregiverId;
        private String caregiverEmail;
        private String newTemporaryPassword;
        private String message;

        public ResetPasswordResultResponse() {}

        public ResetPasswordResultResponse(Long caregiverId, String caregiverEmail, String newTemporaryPassword, String message) {
            this.caregiverId = caregiverId;
            this.caregiverEmail = caregiverEmail;
            this.newTemporaryPassword = newTemporaryPassword;
            this.message = message;
        }

        public Long getCaregiverId() { return caregiverId; }
        public void setCaregiverId(Long caregiverId) { this.caregiverId = caregiverId; }

        public String getCaregiverEmail() { return caregiverEmail; }
        public void setCaregiverEmail(String caregiverEmail) { this.caregiverEmail = caregiverEmail; }

        public String getNewTemporaryPassword() { return newTemporaryPassword; }
        public void setNewTemporaryPassword(String newTemporaryPassword) { this.newTemporaryPassword = newTemporaryPassword; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class PromptHistoryItem {
        private String stepTitle;
        private int stepNumber;
        private String date;
        private String promptLevelName;
        private String promptLevelCode;
        private String observation;

        public PromptHistoryItem() {}

        public PromptHistoryItem(String stepTitle, int stepNumber, String date, String promptLevelName, String promptLevelCode, String observation) {
            this.stepTitle = stepTitle;
            this.stepNumber = stepNumber;
            this.date = date;
            this.promptLevelName = promptLevelName;
            this.promptLevelCode = promptLevelCode;
            this.observation = observation;
        }

        public String getStepTitle() { return stepTitle; }
        public void setStepTitle(String stepTitle) { this.stepTitle = stepTitle; }

        public int getStepNumber() { return stepNumber; }
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getPromptLevelName() { return promptLevelName; }
        public void setPromptLevelName(String promptLevelName) { this.promptLevelName = promptLevelName; }

        public String getPromptLevelCode() { return promptLevelCode; }
        public void setPromptLevelCode(String promptLevelCode) { this.promptLevelCode = promptLevelCode; }

        public String getObservation() { return observation; }
        public void setObservation(String observation) { this.observation = observation; }
    }

    public static class TherapistCaregiverSummaryItem {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private Boolean isActive;
        private String relationship;
        private Long childId;
        private String childName;
        private int activeHomeProgramsCount;
        private int pendingPracticeReviewsCount;

        public TherapistCaregiverSummaryItem() {}

        public TherapistCaregiverSummaryItem(Long id, String fullName, String email, String phone, Boolean isActive, String relationship, Long childId, String childName, int activeHomeProgramsCount, int pendingPracticeReviewsCount) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.isActive = isActive;
            this.relationship = relationship;
            this.childId = childId;
            this.childName = childName;
            this.activeHomeProgramsCount = activeHomeProgramsCount;
            this.pendingPracticeReviewsCount = pendingPracticeReviewsCount;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }

        public int getActiveHomeProgramsCount() { return activeHomeProgramsCount; }
        public void setActiveHomeProgramsCount(int activeHomeProgramsCount) { this.activeHomeProgramsCount = activeHomeProgramsCount; }

        public int getPendingPracticeReviewsCount() { return pendingPracticeReviewsCount; }
        public void setPendingPracticeReviewsCount(int pendingPracticeReviewsCount) { this.pendingPracticeReviewsCount = pendingPracticeReviewsCount; }
    }
}
