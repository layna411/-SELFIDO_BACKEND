package com.simats.selfora.dto;

import java.util.List;

public class ActivityDtos {

    public static class TaskStepResponse {
        private Long id;
        private Long activityId;
        private Integer stepNumber;
        private String title;
        private String instructionText;
        private String childInstruction;
        private String audioPromptUrl;
        private List<MediaResponse> media;

        public TaskStepResponse() {}

        public TaskStepResponse(Long id, Long activityId, Integer stepNumber, String title, String instructionText, String childInstruction, String audioPromptUrl, List<MediaResponse> media) {
            this.id = id;
            this.activityId = activityId;
            this.stepNumber = stepNumber;
            this.title = title;
            this.instructionText = instructionText;
            this.childInstruction = childInstruction;
            this.audioPromptUrl = audioPromptUrl;
            this.media = media;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long activityId;
            private Integer stepNumber;
            private String title;
            private String instructionText;
            private String childInstruction;
            private String audioPromptUrl;
            private List<MediaResponse> media;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder activityId(Long activityId) { this.activityId = activityId; return this; }
            public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
            public Builder title(String title) { this.title = title; return this; }
            public Builder instructionText(String instructionText) { this.instructionText = instructionText; return this; }
            public Builder childInstruction(String childInstruction) { this.childInstruction = childInstruction; return this; }
            public Builder audioPromptUrl(String audioPromptUrl) { this.audioPromptUrl = audioPromptUrl; return this; }
            public Builder media(List<MediaResponse> media) { this.media = media; return this; }
            public TaskStepResponse build() { return new TaskStepResponse(id, activityId, stepNumber, title, instructionText, childInstruction, audioPromptUrl, media); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getInstructionText() { return instructionText; }
        public void setInstructionText(String instructionText) { this.instructionText = instructionText; }
        public String getChildInstruction() { return childInstruction; }
        public void setChildInstruction(String childInstruction) { this.childInstruction = childInstruction; }
        public String getAudioPromptUrl() { return audioPromptUrl; }
        public void setAudioPromptUrl(String audioPromptUrl) { this.audioPromptUrl = audioPromptUrl; }
        public List<MediaResponse> getMedia() { return media; }
        public void setMedia(List<MediaResponse> media) { this.media = media; }
    }

    public static class MediaResponse {
        private Long id;
        private String mediaType;
        private String mediaUrl;
        private String thumbnailUrl;
        private String caption;

        public MediaResponse() {}

        public MediaResponse(Long id, String mediaType, String mediaUrl, String thumbnailUrl, String caption) {
            this.id = id;
            this.mediaType = mediaType;
            this.mediaUrl = mediaUrl;
            this.thumbnailUrl = thumbnailUrl;
            this.caption = caption;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private String mediaType;
            private String mediaUrl;
            private String thumbnailUrl;
            private String caption;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder mediaType(String mediaType) { this.mediaType = mediaType; return this; }
            public Builder mediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; return this; }
            public Builder thumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; return this; }
            public Builder caption(String caption) { this.caption = caption; return this; }
            public MediaResponse build() { return new MediaResponse(id, mediaType, mediaUrl, thumbnailUrl, caption); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        public String getMediaUrl() { return mediaUrl; }
        public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
    }

    public static class ActivityResponse {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String targetGender;
        private String description;
        private String iconUrl;
        private Integer totalSteps;
        private List<TaskStepResponse> steps;

        public ActivityResponse() {}

        public ActivityResponse(Long id, Long categoryId, String categoryName, String title, String targetGender, String description, String iconUrl, Integer totalSteps, List<TaskStepResponse> steps) {
            this.id = id;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.title = title;
            this.targetGender = targetGender;
            this.description = description;
            this.iconUrl = iconUrl;
            this.totalSteps = totalSteps;
            this.steps = steps;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private Long categoryId;
            private String categoryName;
            private String title;
            private String targetGender;
            private String description;
            private String iconUrl;
            private Integer totalSteps;
            private List<TaskStepResponse> steps;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
            public Builder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
            public Builder title(String title) { this.title = title; return this; }
            public Builder targetGender(String targetGender) { this.targetGender = targetGender; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder iconUrl(String iconUrl) { this.iconUrl = iconUrl; return this; }
            public Builder totalSteps(Integer totalSteps) { this.totalSteps = totalSteps; return this; }
            public Builder steps(List<TaskStepResponse> steps) { this.steps = steps; return this; }
            public ActivityResponse build() { return new ActivityResponse(id, categoryId, categoryName, title, targetGender, description, iconUrl, totalSteps, steps); }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTargetGender() { return targetGender; }
        public void setTargetGender(String targetGender) { this.targetGender = targetGender; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIconUrl() { return iconUrl; }
        public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
        public Integer getTotalSteps() { return totalSteps; }
        public void setTotalSteps(Integer totalSteps) { this.totalSteps = totalSteps; }
        public List<TaskStepResponse> getSteps() { return steps; }
        public void setSteps(List<TaskStepResponse> steps) { this.steps = steps; }
    }

    public static class MicroSkillResponse {
        private Long id;
        private String name;
        private Integer sequenceOrder;
        private String description;

        public MicroSkillResponse() {}

        public MicroSkillResponse(Long id, String name, Integer sequenceOrder, String description) {
            this.id = id;
            this.name = name;
            this.sequenceOrder = sequenceOrder;
            this.description = description;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
