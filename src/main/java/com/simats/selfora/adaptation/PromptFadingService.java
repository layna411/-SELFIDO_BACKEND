package com.simats.selfora.adaptation;

import com.simats.selfora.child.ChildService;
import com.simats.selfora.dto.PromptFadingDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromptFadingService {

    @Autowired
    private PromptFadingPlanRepository promptFadingPlanRepository;

    @Autowired
    private PromptFadingHistoryRepository promptFadingHistoryRepository;

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

    @Autowired
    private ChildService childService;

    public PromptFadingDtos.PromptFadingPlanResponse createPromptFadingPlan(PromptFadingDtos.CreatePromptFadingPlanRequest request) {
        childService.validateChildAccessForCurrentUser(request.getChildId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        TaskStep step = taskStepRepository.findById(request.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Task step not found"));

        PromptLevel currentPrompt = promptLevelRepository.findById(request.getCurrentPromptLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Current prompt level not found"));

        PromptLevel targetPrompt = promptLevelRepository.findById(request.getTargetPromptLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Target prompt level not found"));

        PromptFadingPlan plan = PromptFadingPlan.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .step(step)
                .currentPromptLevel(currentPrompt)
                .targetPromptLevel(targetPrompt)
                .clinicalReason(request.getClinicalReason())
                .reviewCriteria(request.getReviewCriteria())
                .notes(request.getNotes())
                .status("UNDER_REVIEW")
                .build();

        PromptFadingPlan saved = promptFadingPlanRepository.save(plan);
        return mapToPlanResponse(saved);
    }

    public PromptFadingDtos.PromptFadingPlanResponse approvePromptFadingPlan(Long planId) {
        PromptFadingPlan plan = promptFadingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt fading plan not found: " + planId));

        childService.validateChildAccessForCurrentUser(plan.getChild().getId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        plan.setStatus("APPROVED");
        plan.setApprovedByTherapist(therapist);
        plan.setApprovedAt(LocalDateTime.now());

        PromptFadingPlan saved = promptFadingPlanRepository.save(plan);

        // Record in auditable history
        PromptFadingHistory history = PromptFadingHistory.builder()
                .child(plan.getChild())
                .step(plan.getStep())
                .approvedByTherapist(therapist)
                .previousPromptLevel(plan.getCurrentPromptLevel())
                .newPromptLevel(plan.getTargetPromptLevel())
                .changeDate(LocalDateTime.now())
                .reason(plan.getClinicalReason())
                .subsequentPerformanceNotes("Plan approved by therapist " + therapist.getFirstName() + " " + therapist.getLastName())
                .build();

        promptFadingHistoryRepository.save(history);

        return mapToPlanResponse(saved);
    }

    public PromptFadingDtos.PromptFadingPlanResponse updatePlanStatus(Long planId, String status) {
        PromptFadingPlan plan = promptFadingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt fading plan not found: " + planId));

        childService.validateChildAccessForCurrentUser(plan.getChild().getId());

        plan.setStatus(status.toUpperCase());
        PromptFadingPlan saved = promptFadingPlanRepository.save(plan);
        return mapToPlanResponse(saved);
    }

    public List<PromptFadingDtos.PromptFadingPlanResponse> getFadingPlansForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);
        return promptFadingPlanRepository.findByChildIdOrderByCreatedAtDesc(childId).stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    public List<PromptFadingDtos.PromptFadingHistoryItemResponse> getFadingHistoryForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);
        return promptFadingHistoryRepository.findByChildIdOrderByChangeDateDesc(childId).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    private PromptFadingDtos.PromptFadingPlanResponse mapToPlanResponse(PromptFadingPlan p) {
        String approvedBy = p.getApprovedByTherapist() != null
                ? p.getApprovedByTherapist().getFirstName() + " " + p.getApprovedByTherapist().getLastName()
                : null;

        return PromptFadingDtos.PromptFadingPlanResponse.builder()
                .id(p.getId())
                .childId(p.getChild().getId())
                .childName(p.getChild().getFirstName() + " " + p.getChild().getLastName())
                .activityId(p.getActivity().getId())
                .activityTitle(p.getActivity().getTitle())
                .stepId(p.getStep().getId())
                .stepNumber(p.getStep().getStepNumber())
                .stepTitle(p.getStep().getTitle())
                .currentPromptLevelId(p.getCurrentPromptLevel().getId())
                .currentPromptLevelName(p.getCurrentPromptLevel().getLevelName())
                .targetPromptLevelId(p.getTargetPromptLevel().getId())
                .targetPromptLevelName(p.getTargetPromptLevel().getLevelName())
                .clinicalReason(p.getClinicalReason())
                .reviewCriteria(p.getReviewCriteria())
                .notes(p.getNotes())
                .status(p.getStatus())
                .approvedByTherapistName(approvedBy)
                .approvedAt(p.getApprovedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PromptFadingDtos.PromptFadingHistoryItemResponse mapToHistoryResponse(PromptFadingHistory h) {
        String approvedBy = h.getApprovedByTherapist() != null
                ? h.getApprovedByTherapist().getFirstName() + " " + h.getApprovedByTherapist().getLastName()
                : "Therapist";

        return PromptFadingDtos.PromptFadingHistoryItemResponse.builder()
                .id(h.getId())
                .childId(h.getChild().getId())
                .stepId(h.getStep().getId())
                .stepNumber(h.getStep().getStepNumber())
                .stepTitle(h.getStep().getTitle())
                .approvedByTherapistName(approvedBy)
                .previousPromptLevelName(h.getPreviousPromptLevel().getLevelName())
                .newPromptLevelName(h.getNewPromptLevel().getLevelName())
                .changeDate(h.getChangeDate())
                .reason(h.getReason())
                .subsequentPerformanceNotes(h.getSubsequentPerformanceNotes())
                .build();
    }
}
