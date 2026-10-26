package com.simats.selfora.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminDtos {

    public static class DashboardStats {
        private long totalTherapists;
        private long activeTherapists;
        private long totalChildren;
        private long totalCaregivers;
        private long activeTherapySessions;
        private long completedAssessments;
        private long activeHomeProgrammes;
        private long pendingCaregiverReviews;

        public DashboardStats() {}

        public DashboardStats(long totalTherapists, long activeTherapists, long totalChildren, long totalCaregivers, long activeTherapySessions, long completedAssessments, long activeHomeProgrammes, long pendingCaregiverReviews) {
            this.totalTherapists = totalTherapists;
            this.activeTherapists = activeTherapists;
            this.totalChildren = totalChildren;
            this.totalCaregivers = totalCaregivers;
            this.activeTherapySessions = activeTherapySessions;
            this.completedAssessments = completedAssessments;
            this.activeHomeProgrammes = activeHomeProgrammes;
            this.pendingCaregiverReviews = pendingCaregiverReviews;
        }

        public long getTotalTherapists() { return totalTherapists; }
        public void setTotalTherapists(long totalTherapists) { this.totalTherapists = totalTherapists; }

        public long getActiveTherapists() { return activeTherapists; }
        public void setActiveTherapists(long activeTherapists) { this.activeTherapists = activeTherapists; }

        public long getTotalChildren() { return totalChildren; }
        public void setTotalChildren(long totalChildren) { this.totalChildren = totalChildren; }

        public long getTotalCaregivers() { return totalCaregivers; }
        public void setTotalCaregivers(long totalCaregivers) { this.totalCaregivers = totalCaregivers; }

        public long getActiveTherapySessions() { return activeTherapySessions; }
        public void setActiveTherapySessions(long activeTherapySessions) { this.activeTherapySessions = activeTherapySessions; }

        public long getCompletedAssessments() { return completedAssessments; }
        public void setCompletedAssessments(long completedAssessments) { this.completedAssessments = completedAssessments; }

        public long getActiveHomeProgrammes() { return activeHomeProgrammes; }
        public void setActiveHomeProgrammes(long activeHomeProgrammes) { this.activeHomeProgrammes = activeHomeProgrammes; }

        public long getPendingCaregiverReviews() { return pendingCaregiverReviews; }
        public void setPendingCaregiverReviews(long pendingCaregiverReviews) { this.pendingCaregiverReviews = pendingCaregiverReviews; }
    }

    public static class ChartData {
        private Map<String, Long> registrationsOverTime;
        private Map<String, Long> sessionsOverTime;
        private Map<String, Long> adlCategoryDistribution;

        public ChartData() {}

        public ChartData(Map<String, Long> registrationsOverTime, Map<String, Long> sessionsOverTime, Map<String, Long> adlCategoryDistribution) {
            this.registrationsOverTime = registrationsOverTime;
            this.sessionsOverTime = sessionsOverTime;
            this.adlCategoryDistribution = adlCategoryDistribution;
        }

        public Map<String, Long> getRegistrationsOverTime() { return registrationsOverTime; }
        public void setRegistrationsOverTime(Map<String, Long> registrationsOverTime) { this.registrationsOverTime = registrationsOverTime; }

        public Map<String, Long> getSessionsOverTime() { return sessionsOverTime; }
        public void setSessionsOverTime(Map<String, Long> sessionsOverTime) { this.sessionsOverTime = sessionsOverTime; }

        public Map<String, Long> getAdlCategoryDistribution() { return adlCategoryDistribution; }
        public void setAdlCategoryDistribution(Map<String, Long> adlCategoryDistribution) { this.adlCategoryDistribution = adlCategoryDistribution; }
    }

    public static class TherapistCreateRequest {
        private String fullName;
        private String email;
        private String phone;
        private String username;
        private String password;
        private String designation;
        private String specialization;
        private String qualification;
        private String experience;

        public TherapistCreateRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }

        public String getQualification() { return qualification; }
        public void setQualification(String qualification) { this.qualification = qualification; }

        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }
    }

    public static class TherapistProfile {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private String designation;
        private String specialization;
        private String qualification;
        private String experience;
        private Boolean isActive;
        private Boolean mustChangePassword;
        private LocalDateTime createdAt;
        private long assignedChildrenCount;
        private long totalSessions;

        public TherapistProfile() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }

        public String getQualification() { return qualification; }
        public void setQualification(String qualification) { this.qualification = qualification; }

        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }

        public Boolean getMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public long getAssignedChildrenCount() { return assignedChildrenCount; }
        public void setAssignedChildrenCount(long assignedChildrenCount) { this.assignedChildrenCount = assignedChildrenCount; }

        public long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }
    }

    public static class TherapistUpdateRequest {
        private String fullName;
        private String email;
        private String phone;
        private String designation;
        private String specialization;
        private String qualification;
        private String experience;

        public TherapistUpdateRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }

        public String getQualification() { return qualification; }
        public void setQualification(String qualification) { this.qualification = qualification; }

        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }
    }

    public static class ChildReassignRequest {
        private Long childId;
        private Long newTherapistId;
        private String reason;

        public ChildReassignRequest() {}

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public Long getNewTherapistId() { return newTherapistId; }
        public void setNewTherapistId(Long newTherapistId) { this.newTherapistId = newTherapistId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class AdminChildSummary {
        private Long id;
        private String fullName;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private Integer age;
        private String gender;
        private String avatarUrl;
        private Boolean isActive;
        private Long assignedTherapistId;
        private String assignedTherapistName;
        private Long linkedCaregiverId;
        private String linkedCaregiverName;
        private String activeAdlCategory;
        private LocalDateTime createdAt;

        public AdminChildSummary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }

        public Long getAssignedTherapistId() { return assignedTherapistId; }
        public void setAssignedTherapistId(Long assignedTherapistId) { this.assignedTherapistId = assignedTherapistId; }

        public String getAssignedTherapistName() { return assignedTherapistName; }
        public void setAssignedTherapistName(String assignedTherapistName) { this.assignedTherapistName = assignedTherapistName; }

        public Long getLinkedCaregiverId() { return linkedCaregiverId; }
        public void setLinkedCaregiverId(Long linkedCaregiverId) { this.linkedCaregiverId = linkedCaregiverId; }

        public String getLinkedCaregiverName() { return linkedCaregiverName; }
        public void setLinkedCaregiverName(String linkedCaregiverName) { this.linkedCaregiverName = linkedCaregiverName; }

        public String getActiveAdlCategory() { return activeAdlCategory; }
        public void setActiveAdlCategory(String activeAdlCategory) { this.activeAdlCategory = activeAdlCategory; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class AdminChildDetailProfile {
        private AdminChildSummary summary;
        private String diagnosisNotes;
        private List<String> supportedAdlCategories;
        private long totalSessions;
        private long completedAssessments;
        private String activeHomeProgramTitle;
        private long homePracticeSessionsCount;

        public AdminChildDetailProfile() {}

        public AdminChildSummary getSummary() { return summary; }
        public void setSummary(AdminChildSummary summary) { this.summary = summary; }

        public String getDiagnosisNotes() { return diagnosisNotes; }
        public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }

        public List<String> getSupportedAdlCategories() { return supportedAdlCategories; }
        public void setSupportedAdlCategories(List<String> supportedAdlCategories) { this.supportedAdlCategories = supportedAdlCategories; }

        public long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

        public long getCompletedAssessments() { return completedAssessments; }
        public void setCompletedAssessments(long completedAssessments) { this.completedAssessments = completedAssessments; }

        public String getActiveHomeProgramTitle() { return activeHomeProgramTitle; }
        public void setActiveHomeProgramTitle(String activeHomeProgramTitle) { this.activeHomeProgramTitle = activeHomeProgramTitle; }

        public long getHomePracticeSessionsCount() { return homePracticeSessionsCount; }
        public void setHomePracticeSessionsCount(long homePracticeSessionsCount) { this.homePracticeSessionsCount = homePracticeSessionsCount; }
    }

    public static class CaregiverCreateRequest {
        private String fullName;
        private String email;
        private String phone;
        private String username;
        private String password;

        public CaregiverCreateRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CaregiverUpdateRequest {
        private String fullName;
        private String email;
        private String phone;

        public CaregiverUpdateRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ResetPasswordRequestBody {
        private String newPassword;

        public ResetPasswordRequestBody() {}
        public ResetPasswordRequestBody(String newPassword) { this.newPassword = newPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class CaregiverProfile {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private Boolean isActive;
        private Boolean mustChangePassword;
        private LocalDateTime createdAt;
        private long linkedChildrenCount;
        private List<String> linkedChildrenNames;
        private List<AdminChildSummary> linkedChildren;

        public CaregiverProfile() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }

        public Boolean getMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public long getLinkedChildrenCount() { return linkedChildrenCount; }
        public void setLinkedChildrenCount(long linkedChildrenCount) { this.linkedChildrenCount = linkedChildrenCount; }

        public List<String> getLinkedChildrenNames() { return linkedChildrenNames; }
        public void setLinkedChildrenNames(List<String> linkedChildrenNames) { this.linkedChildrenNames = linkedChildrenNames; }

        public List<AdminChildSummary> getLinkedChildren() { return linkedChildren; }
        public void setLinkedChildren(List<AdminChildSummary> linkedChildren) { this.linkedChildren = linkedChildren; }
    }
}
