package com.simats.selfora.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChildModeDtos {

    public static class ChildDashboardResponse {
        private Long childId;
        private String childName;
        private String preferredName;
        private String avatarUrl;
        private String gender;
        private List<ChildActivitySummaryDto> assignedActivities;
        private ChildActivitySummaryDto continueActivity;
        private List<ChildActivitySummaryDto> allActivities;
        private List<ChildRewardDto> earnedRewards;
        private int totalStars;
        private int completedCount;
        private ChildModePreferenceDto preferences;

        public ChildDashboardResponse() {}

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public String getChildName() { return childName; }
        public void setChildName(String childName) { this.childName = childName; }

        public String getPreferredName() { return preferredName; }
        public void setPreferredName(String preferredName) { this.preferredName = preferredName; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public List<ChildActivitySummaryDto> getAssignedActivities() { return assignedActivities; }
        public void setAssignedActivities(List<ChildActivitySummaryDto> assignedActivities) { this.assignedActivities = assignedActivities; }

        public ChildActivitySummaryDto getContinueActivity() { return continueActivity; }
        public void setContinueActivity(ChildActivitySummaryDto continueActivity) { this.continueActivity = continueActivity; }

        public List<ChildActivitySummaryDto> getAllActivities() { return allActivities; }
        public void setAllActivities(List<ChildActivitySummaryDto> allActivities) { this.allActivities = allActivities; }

        public List<ChildRewardDto> getEarnedRewards() { return earnedRewards; }
        public void setEarnedRewards(List<ChildRewardDto> earnedRewards) { this.earnedRewards = earnedRewards; }

        public int getTotalStars() { return totalStars; }
        public void setTotalStars(int totalStars) { this.totalStars = totalStars; }

        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

        public ChildModePreferenceDto getPreferences() { return preferences; }
        public void setPreferences(ChildModePreferenceDto preferences) { this.preferences = preferences; }
    }

    public static class ChildActivitySummaryDto {
        private Long activityId;
        private String title;
        private String description;
        private String iconUrl;
        private String categoryName;
        private int totalSteps;
        private int completedSteps;
        private boolean isAssigned;
        private boolean isCompleted;
        private int starsEarned;

        public ChildActivitySummaryDto() {}

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public int getTotalSteps() { return totalSteps; }
        public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

        public int getCompletedSteps() { return completedSteps; }
        public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }

        public boolean isAssigned() { return isAssigned; }
        public void setAssigned(boolean assigned) { isAssigned = assigned; }

        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }

        public int getStarsEarned() { return starsEarned; }
        public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }
    }

    public static class RecordStepProgressRequest {
        private Long childId;
        private Long activityId;
        private Long stepId;
        private Integer stepNumber;
        private Boolean isActivityCompleted;

        public RecordStepProgressRequest() {}

        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public Long getStepId() { return stepId; }
        public void setStepId(Long stepId) { this.stepId = stepId; }

        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

        public Boolean getIsActivityCompleted() { return isActivityCompleted; }
        public void setIsActivityCompleted(Boolean isActivityCompleted) { this.isActivityCompleted = isActivityCompleted; }
    }

    public static class ChildRewardDto {
        private Long id;
        private String badgeType;
        private String badgeTitle;
        private String badgeIcon;
        private LocalDateTime earnedAt;

        public ChildRewardDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getBadgeType() { return badgeType; }
        public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

        public String getBadgeTitle() { return badgeTitle; }
        public void setBadgeTitle(String badgeTitle) { this.badgeTitle = badgeTitle; }

        public String getBadgeIcon() { return badgeIcon; }
        public void setBadgeIcon(String badgeIcon) { this.badgeIcon = badgeIcon; }

        public LocalDateTime getEarnedAt() { return earnedAt; }
        public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
    }

    public static class ChildModePreferenceDto {
        private String preferredName;
        private Boolean audioEnabled = true;
        private Boolean animationEnabled = true;
        private Boolean reducedMotion = false;
        private String language = "EN";
        private Boolean rewardSounds = true;

        public ChildModePreferenceDto() {}

        public String getPreferredName() { return preferredName; }
        public void setPreferredName(String preferredName) { this.preferredName = preferredName; }

        public Boolean getAudioEnabled() { return audioEnabled; }
        public void setAudioEnabled(Boolean audioEnabled) { this.audioEnabled = audioEnabled; }

        public Boolean getAnimationEnabled() { return animationEnabled; }
        public void setAnimationEnabled(Boolean animationEnabled) { this.animationEnabled = animationEnabled; }

        public Boolean getReducedMotion() { return reducedMotion; }
        public void setReducedMotion(Boolean reducedMotion) { this.reducedMotion = reducedMotion; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public Boolean getRewardSounds() { return rewardSounds; }
        public void setRewardSounds(Boolean rewardSounds) { this.rewardSounds = rewardSounds; }
    }
}
