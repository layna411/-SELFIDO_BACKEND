package com.simats.selfora.homeprogram;

import com.simats.selfora.dto.ActivityDtos;
import com.simats.selfora.dto.HomeProgramDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeProgramService {

    @Autowired
    private HomeProgramRepository homeProgramRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private PromptLevelRepository promptLevelRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public HomeProgramDtos.HomeProgramResponse createHomeProgram(HomeProgramDtos.CreateHomeProgramRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        PromptLevel promptLevel = promptLevelRepository.findById(request.getTargetPromptLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Prompt level not found"));

        Set<TaskStep> steps = new HashSet<>();
        if (request.getTargetStepIds() != null && !request.getTargetStepIds().isEmpty()) {
            steps.addAll(taskStepRepository.findAllById(request.getTargetStepIds()));
        } else {
            steps.addAll(taskStepRepository.findByActivityIdOrderByStepNumberAsc(activity.getId()));
        }

        HomeProgram program = HomeProgram.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .frequencyPerWeek(request.getFrequencyPerWeek() != null ? request.getFrequencyPerWeek() : 3)
                .targetDurationMinutes(request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 15)
                .targetPromptLevel(promptLevel)
                .goalStatement(request.getGoalStatement())
                .caregiverInstructions(request.getCaregiverInstructions())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate() != null ? request.getEndDate() : LocalDate.now().plusWeeks(2))
                .targetSteps(steps)
                .isActive(true)
                .build();

        HomeProgram saved = homeProgramRepository.save(program);
        return mapToResponse(saved);
    }

    public HomeProgramDtos.HomeProgramResponse getActiveHomeProgramForChild(Long childId) {
        HomeProgram program = homeProgramRepository.findTopByChildIdAndIsActiveTrueOrderByCreatedAtDesc(childId)
                .orElseThrow(() -> new ResourceNotFoundException("No active home program assigned for child " + childId));
        return mapToResponse(program);
    }

    public List<HomeProgramDtos.HomeProgramResponse> getHomeProgramsForCaregiver() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User caregiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver user not found"));

        List<Child> children = childRepository.findByCaregiverId(caregiver.getId());
        List<HomeProgram> programs = new ArrayList<>();
        for (Child c : children) {
            homeProgramRepository.findTopByChildIdAndIsActiveTrueOrderByCreatedAtDesc(c.getId())
                    .ifPresent(programs::add);
        }

        return programs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private HomeProgramDtos.HomeProgramResponse mapToResponse(HomeProgram hp) {
        List<ActivityDtos.TaskStepResponse> stepDtos = hp.getTargetSteps().stream()
                .sorted(Comparator.comparingInt(TaskStep::getStepNumber))
                .map(s -> ActivityDtos.TaskStepResponse.builder()
                        .id(s.getId())
                        .activityId(s.getActivity().getId())
                        .stepNumber(s.getStepNumber())
                        .title(s.getTitle())
                        .instructionText(s.getInstructionText())
                        .childInstruction(s.getChildInstruction())
                        .audioPromptUrl(s.getAudioPromptUrl())
                        .media(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());

        return HomeProgramDtos.HomeProgramResponse.builder()
                .id(hp.getId())
                .childId(hp.getChild().getId())
                .childName(hp.getChild().getFirstName() + " " + hp.getChild().getLastName())
                .therapistId(hp.getTherapist().getId())
                .therapistName(hp.getTherapist().getFullName())
                .activityId(hp.getActivity().getId())
                .activityTitle(hp.getActivity().getTitle())
                .frequencyPerWeek(hp.getFrequencyPerWeek())
                .targetDurationMinutes(hp.getTargetDurationMinutes())
                .targetPromptLevelId(hp.getTargetPromptLevel().getId())
                .targetPromptLevelName(hp.getTargetPromptLevel().getLevelName())
                .goalStatement(hp.getGoalStatement())
                .caregiverInstructions(hp.getCaregiverInstructions())
                .startDate(hp.getStartDate())
                .endDate(hp.getEndDate())
                .isActive(hp.getIsActive())
                .targetSteps(stepDtos)
                .createdAt(hp.getCreatedAt())
                .build();
    }
}
