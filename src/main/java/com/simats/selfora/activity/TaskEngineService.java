package com.simats.selfora.activity;

import com.simats.selfora.dto.ActivityDtos;
import com.simats.selfora.entity.Activity;
import com.simats.selfora.entity.TaskStep;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.ActivityRepository;
import com.simats.selfora.repository.TaskStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskEngineService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    public List<ActivityDtos.ActivityResponse> getAllActiveActivities() {
        return activityRepository.findByIsActiveTrue().stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityDtos.ActivityResponse> getActivitiesForGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return getAllActiveActivities();
        }
        String normalizedGender = normalizeGender(gender);
        return activityRepository.findByIsActiveTrue().stream()
                .filter(a -> isActivityMatchingGender(a, normalizedGender))
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    public boolean isActivityMatchingGender(Activity a, String childGender) {
        String target = a.getTargetGender() != null ? a.getTargetGender().trim().toUpperCase() : "ALL";
        String categorySub = a.getCategory() != null && a.getCategory().getSubCategory() != null
                ? a.getCategory().getSubCategory().toUpperCase() : "";

        if (target.equals("ALL") || target.equals("COMMON")) {
            return true;
        }

        if ("BOY".equals(childGender)) {
            if (target.equals("BOYS") || target.equals("MALE") || target.equals("BOY")) return true;
            if (categorySub.contains("BOYS") || categorySub.contains("MALE")) return true;
            return false;
        } else if ("GIRL".equals(childGender)) {
            if (target.equals("GIRLS") || target.equals("FEMALE") || target.equals("GIRL")) return true;
            if (categorySub.contains("GIRLS") || categorySub.contains("FEMALE")) return true;
            return false;
        }
        return true;
    }

    public static String normalizeGender(String raw) {
        if (raw == null) return "BOY";
        String upper = raw.trim().toUpperCase();
        if (upper.startsWith("G") || upper.startsWith("F")) {
            return "GIRL";
        }
        return "BOY";
    }

    public ActivityDtos.ActivityResponse getActivityById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + id));
        return mapToActivityResponse(activity);
    }

    public List<ActivityDtos.TaskStepResponse> getTaskStepsForActivity(Long activityId) {
        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activityId);
        return steps.stream().map(this::mapToTaskStepResponse).collect(Collectors.toList());
    }

    private ActivityDtos.ActivityResponse mapToActivityResponse(Activity activity) {
        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activity.getId());
        return ActivityDtos.ActivityResponse.builder()
                .id(activity.getId())
                .categoryId(activity.getCategory().getId())
                .categoryName(activity.getCategory().getName())
                .title(activity.getTitle())
                .titleTa(activity.getTitleTa())
                .targetGender(activity.getTargetGender())
                .startingPosition(activity.getStartingPosition())
                .startingPositionTa(activity.getStartingPositionTa())
                .description(activity.getDescription())
                .descriptionTa(activity.getDescriptionTa())
                .iconUrl(activity.getIconUrl())
                .totalSteps(steps.size())
                .steps(steps.stream().map(this::mapToTaskStepResponse).collect(Collectors.toList()))
                .build();
    }

    private ActivityDtos.TaskStepResponse mapToTaskStepResponse(TaskStep step) {
        return ActivityDtos.TaskStepResponse.builder()
                .id(step.getId())
                .activityId(step.getActivity().getId())
                .stepNumber(step.getStepNumber())
                .title(step.getTitle())
                .titleTa(step.getTitleTa())
                .instructionText(step.getInstructionText())
                .instructionTextTa(step.getInstructionTextTa())
                .childInstruction(step.getChildInstruction())
                .childInstructionTa(step.getChildInstructionTa())
                .audioPromptUrl(step.getAudioPromptUrl())
                .audioPromptUrlTa(step.getAudioPromptUrlTa())
                .media(Collections.emptyList())
                .build();
    }
}
