package com.simats.selfora.dto;

import com.simats.selfora.dto.CaregiverPracticeDtos.CaregiverPracticeSessionResponse;
import com.simats.selfora.dto.HomeProgramDtos.HomeProgramResponse;
import java.util.List;

public class CaregiverDtos {

    public static class LinkedChildDto {
        private Long childId;
        private String name;
        private String gender;
        private int age;
        private String therapistName;
        private String profilePicUrl;

        public LinkedChildDto() {}

        public LinkedChildDto(Long childId, String name, String gender, int age, String therapistName, String profilePicUrl) {
            this.childId = childId;
            this.name = name;
            this.gender = gender;
            this.age = age;
            this.therapistName = therapistName;
            this.profilePicUrl = profilePicUrl;
        }

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getTherapistName() { return therapistName; }
        public void setTherapistName(String therapistName) { this.therapistName = therapistName; }

        public String getProfilePicUrl() { return profilePicUrl; }
        public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
    }

    public static class CaregiverDashboardSummaryResponse {
        private String caregiverName;
        private String caregiverEmail;
        private List<LinkedChildDto> linkedChildren;
        private int totalCompletedPractices;
        private int activeProgramsCount;
        private List<HomeProgramResponse> todayHomePrograms;
        private List<CaregiverPracticeSessionResponse> recentPracticeHistory;

        public CaregiverDashboardSummaryResponse() {}

        public CaregiverDashboardSummaryResponse(String caregiverName, String caregiverEmail, List<LinkedChildDto> linkedChildren, int totalCompletedPractices, int activeProgramsCount, List<HomeProgramResponse> todayHomePrograms, List<CaregiverPracticeSessionResponse> recentPracticeHistory) {
            this.caregiverName = caregiverName;
            this.caregiverEmail = caregiverEmail;
            this.linkedChildren = linkedChildren;
            this.totalCompletedPractices = totalCompletedPractices;
            this.activeProgramsCount = activeProgramsCount;
            this.todayHomePrograms = todayHomePrograms;
            this.recentPracticeHistory = recentPracticeHistory;
        }

        public String getCaregiverName() { return caregiverName; }
        public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }

        public String getCaregiverEmail() { return caregiverEmail; }
        public void setCaregiverEmail(String caregiverEmail) { this.caregiverEmail = caregiverEmail; }

        public List<LinkedChildDto> getLinkedChildren() { return linkedChildren; }
        public void setLinkedChildren(List<LinkedChildDto> linkedChildren) { this.linkedChildren = linkedChildren; }

        public int getTotalCompletedPractices() { return totalCompletedPractices; }
        public void setTotalCompletedPractices(int totalCompletedPractices) { this.totalCompletedPractices = totalCompletedPractices; }

        public int getActiveProgramsCount() { return activeProgramsCount; }
        public void setActiveProgramsCount(int activeProgramsCount) { this.activeProgramsCount = activeProgramsCount; }

        public List<HomeProgramResponse> getTodayHomePrograms() { return todayHomePrograms; }
        public void setTodayHomePrograms(List<HomeProgramResponse> todayHomePrograms) { this.todayHomePrograms = todayHomePrograms; }

        public List<CaregiverPracticeSessionResponse> getRecentPracticeHistory() { return recentPracticeHistory; }
        public void setRecentPracticeHistory(List<CaregiverPracticeSessionResponse> recentPracticeHistory) { this.recentPracticeHistory = recentPracticeHistory; }
    }
}
