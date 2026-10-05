package com.simats.selfora.homeprogram;

import com.simats.selfora.child.ChildService;
import com.simats.selfora.dto.ActivityDtos;
import com.simats.selfora.dto.HomeProgramDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private PromptLevelRepository promptLevelRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChildService childService;

    public HomeProgramDtos.HomeProgramResponse createHomeProgram(HomeProgramDtos.CreateHomeProgramRequest request) {
        childService.validateChildAccessForCurrentUser(request.getChildId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        PromptLevel promptLevel = null;
        if (request.getTargetPromptLevelId() != null) {
            promptLevel = promptLevelRepository.findById(request.getTargetPromptLevelId()).orElse(null);
        }
        if (promptLevel == null) {
            promptLevel = promptLevelRepository.findById(1).orElse(null); // Visual default
        }

        Set<TaskStep> steps = new HashSet<>();
        if (request.getTargetStepIds() != null && !request.getTargetStepIds().isEmpty()) {
            steps.addAll(taskStepRepository.findAllById(request.getTargetStepIds()));
        } else {
            steps.addAll(taskStepRepository.findByActivityIdOrderByStepNumberAsc(activity.getId()));
        }

        String programTitle = request.getTitle() != null ? request.getTitle() : activity.getTitle() + " Practice Programme";

        HomeProgram program = HomeProgram.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .title(programTitle)
                .description(request.getDescription())
                .status("ACTIVE")
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .scheduleType(request.getScheduleType() != null ? request.getScheduleType() : "DAILY")
                .preferredTime(request.getPreferredTime() != null ? request.getPreferredTime() : "Morning / Evening")
                .frequencyPerWeek(request.getFrequencyPerWeek() != null ? request.getFrequencyPerWeek() : 3)
                .targetDurationMinutes(request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 15)
                .targetPromptLevel(promptLevel)
                .goalStatement(request.getGoalStatement())
                .caregiverInstructions(request.getCaregiverInstructions())
                .stepSpecificInstructions(request.getStepSpecificInstructions())
                .safetyConsiderations(request.getSafetyConsiderations())
                .thingsToAvoid(request.getThingsToAvoid())
                .encouragementSuggestions(request.getEncouragementSuggestions())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate() != null ? request.getEndDate() : LocalDate.now().plusDays(30))
                .targetSteps(steps)
                .isActive(true)
                .build();

        HomeProgram saved = homeProgramRepository.save(program);

        // Notify caregiver
        if (child.getCaregiver() != null) {
            notificationService.sendNotification(
                    child.getCaregiver(),
                    "New Home Programme Assigned",
                    "Dr. " + therapist.getLastName() + " assigned a new home programme: " + programTitle,
                    "PROGRAMME_ASSIGNED",
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    public HomeProgramDtos.HomeProgramResponse updateProgramStatus(Long id, String status) {
        HomeProgram program = homeProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Home program not found"));

        childService.validateChildAccessForCurrentUser(program.getChild().getId());

        program.setStatus(status.toUpperCase());
        if ("COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) || "PAUSED".equalsIgnoreCase(status)) {
            program.setIsActive(!"COMPLETED".equalsIgnoreCase(status) && !"CANCELLED".equalsIgnoreCase(status));
        }

        HomeProgram saved = homeProgramRepository.save(program);
        return mapToResponse(saved);
    }

    public HomeProgramDtos.HomeProgramResponse getActiveHomeProgramForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);

        HomeProgram program = homeProgramRepository.findTopByChildIdAndIsActiveTrueOrderByCreatedAtDesc(childId)
                .orElseThrow(() -> new ResourceNotFoundException("No active home program found for child " + childId));

        return mapToResponse(program);
    }

    public List<HomeProgramDtos.HomeProgramResponse> getAllHomeProgramsForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);
        return homeProgramRepository.findByChildId(childId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HomeProgramDtos.HomeProgramResponse> getHomeProgramsForCaregiver() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User caregiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver not found"));

        List<Child> children = childRepository.findByCaregiverId(caregiver.getId());
        List<HomeProgram> programs = new ArrayList<>();
        for (Child child : children) {
            programs.addAll(homeProgramRepository.findByChildId(child.getId()));
        }

        return programs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private HomeProgramDtos.HomeProgramResponse mapToResponse(HomeProgram p) {
        List<ActivityDtos.TaskStepResponse> stepDtos = p.getTargetSteps().stream()
                .sorted(Comparator.comparing(TaskStep::getStepNumber))
                .map(s -> ActivityDtos.TaskStepResponse.builder()
                        .id(s.getId())
                        .activityId(s.getActivity().getId())
                        .stepNumber(s.getStepNumber())
                        .title(s.getTitle())
                        .instructionText(s.getInstructionText())
                        .childInstruction(s.getChildInstruction())
                        .audioPromptUrl(s.getAudioPromptUrl())
                        .build())
                .collect(Collectors.toList());

        String therapistName = p.getTherapist() != null ? p.getTherapist().getFirstName() + " " + p.getTherapist().getLastName() : "Therapist";
        String promptLevelName = p.getTargetPromptLevel() != null ? p.getTargetPromptLevel().getLevelName() : "Visual Prompt";

        return HomeProgramDtos.HomeProgramResponse.builder()
                .id(p.getId())
                .childId(p.getChild().getId())
                .childName(p.getChild().getFirstName() + " " + p.getChild().getLastName())
                .therapistId(p.getTherapist().getId())
                .therapistName(therapistName)
                .activityId(p.getActivity().getId())
                .activityTitle(p.getActivity().getTitle())
                .title(p.getTitle() != null ? p.getTitle() : p.getActivity().getTitle() + " Practice")
                .description(p.getDescription())
                .status(p.getStatus() != null ? p.getStatus() : "ACTIVE")
                .priority(p.getPriority() != null ? p.getPriority() : "NORMAL")
                .scheduleType(p.getScheduleType() != null ? p.getScheduleType() : "DAILY")
                .preferredTime(p.getPreferredTime())
                .frequencyPerWeek(p.getFrequencyPerWeek())
                .targetDurationMinutes(p.getTargetDurationMinutes())
                .targetPromptLevelId(p.getTargetPromptLevel() != null ? p.getTargetPromptLevel().getId() : 1)
                .targetPromptLevelName(promptLevelName)
                .goalStatement(p.getGoalStatement())
                .caregiverInstructions(p.getCaregiverInstructions())
                .stepSpecificInstructions(p.getStepSpecificInstructions())
                .safetyConsiderations(p.getSafetyConsiderations())
                .thingsToAvoid(p.getThingsToAvoid())
                .encouragementSuggestions(p.getEncouragementSuggestions())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .isActive(p.getIsActive())
                .targetSteps(stepDtos)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
