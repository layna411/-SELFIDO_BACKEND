package com.simats.selfora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_steps")
public class TaskStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "title_ta", length = 150)
    private String titleTa;

    @Column(name = "instruction_text", nullable = false, columnDefinition = "TEXT")
    private String instructionText;

    @Column(name = "instruction_text_ta", columnDefinition = "TEXT")
    private String instructionTextTa;

    @Column(name = "child_instruction", nullable = false, columnDefinition = "TEXT")
    private String childInstruction;

    @Column(name = "child_instruction_ta", columnDefinition = "TEXT")
    private String childInstructionTa;

    @Column(name = "audio_prompt_url", length = 500)
    private String audioPromptUrl;

    @Column(name = "audio_prompt_url_ta", length = 500)
    private String audioPromptUrlTa;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TaskStep() {}

    public TaskStep(Long id, Activity activity, Integer stepNumber, String title, String titleTa, String instructionText, String instructionTextTa, String childInstruction, String childInstructionTa, String audioPromptUrl, String audioPromptUrlTa, LocalDateTime createdAt) {
        this.id = id;
        this.activity = activity;
        this.stepNumber = stepNumber;
        this.title = title;
        this.titleTa = titleTa;
        this.instructionText = instructionText;
        this.instructionTextTa = instructionTextTa;
        this.childInstruction = childInstruction;
        this.childInstructionTa = childInstructionTa;
        this.audioPromptUrl = audioPromptUrl;
        this.audioPromptUrlTa = audioPromptUrlTa;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Activity activity;
        private Integer stepNumber;
        private String title;
        private String titleTa;
        private String instructionText;
        private String instructionTextTa;
        private String childInstruction;
        private String childInstructionTa;
        private String audioPromptUrl;
        private String audioPromptUrlTa;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder activity(Activity activity) { this.activity = activity; return this; }
        public Builder stepNumber(Integer stepNumber) { this.stepNumber = stepNumber; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder titleTa(String titleTa) { this.titleTa = titleTa; return this; }
        public Builder instructionText(String instructionText) { this.instructionText = instructionText; return this; }
        public Builder instructionTextTa(String instructionTextTa) { this.instructionTextTa = instructionTextTa; return this; }
        public Builder childInstruction(String childInstruction) { this.childInstruction = childInstruction; return this; }
        public Builder childInstructionTa(String childInstructionTa) { this.childInstructionTa = childInstructionTa; return this; }
        public Builder audioPromptUrl(String audioPromptUrl) { this.audioPromptUrl = audioPromptUrl; return this; }
        public Builder audioPromptUrlTa(String audioPromptUrlTa) { this.audioPromptUrlTa = audioPromptUrlTa; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TaskStep build() { return new TaskStep(id, activity, stepNumber, title, titleTa, instructionText, instructionTextTa, childInstruction, childInstructionTa, audioPromptUrl, audioPromptUrlTa, createdAt); }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public Integer getStepNumber() { return stepNumber; }
    public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getInstructionText() { return instructionText; }
    public void setInstructionText(String instructionText) { this.instructionText = instructionText; }

    public String getInstructionTextTa() { return instructionTextTa; }
    public void setInstructionTextTa(String instructionTextTa) { this.instructionTextTa = instructionTextTa; }

    public String getChildInstruction() { return childInstruction; }
    public void setChildInstruction(String childInstruction) { this.childInstruction = childInstruction; }

    public String getChildInstructionTa() { return childInstructionTa; }
    public void setChildInstructionTa(String childInstructionTa) { this.childInstructionTa = childInstructionTa; }

    public String getAudioPromptUrl() { return audioPromptUrl; }
    public void setAudioPromptUrl(String audioPromptUrl) { this.audioPromptUrl = audioPromptUrl; }

    public String getAudioPromptUrlTa() { return audioPromptUrlTa; }
    public void setAudioPromptUrlTa(String audioPromptUrlTa) { this.audioPromptUrlTa = audioPromptUrlTa; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
