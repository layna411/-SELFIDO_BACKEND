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
                .targetGender(activity.getTargetGender())
                .description(activity.getDescription())
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
                .instructionText(step.getInstructionText())
                .childInstruction(step.getChildInstruction())
                .audioPromptUrl(step.getAudioPromptUrl())
                .media(Collections.emptyList())
                .build();
    }
}
