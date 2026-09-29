package com.simats.selfora.adaptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.AdaptivePlanDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptivePlanService {

    @Autowired
    private AdaptivePlanRepository adaptivePlanRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired
    private PromptLevelRepository promptLevelRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdaptivePlanDtos.AdaptivePlanResponse createOrConfirmAdaptivePlan(AdaptivePlanDtos.CreateAdaptivePlanRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        TherapySession session = therapySessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        PromptLevel currentPrompt = promptLevelRepository.findById(request.getCurrentPromptLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Current prompt level not found"));

        PromptLevel targetPrompt = promptLevelRepository.findById(request.getTargetPromptLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Target prompt level not found"));

        String targetStepJson = "[]";
        try {
            targetStepJson = objectMapper.writeValueAsString(request.getTargetStepIds());
        } catch (Exception e) {
            targetStepJson = "[]";
        }

        AdaptivePlan plan = AdaptivePlan.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .session(session)
                .currentPromptLevel(currentPrompt)
                .targetPromptLevel(targetPrompt)
                .targetStepIds(targetStepJson)
                .clinicalRationale(request.getClinicalRationale())
                .isConfirmed(request.getIsConfirmed() != null ? request.getIsConfirmed() : true)
                .build();

        AdaptivePlan saved = adaptivePlanRepository.save(plan);
        return mapToResponse(saved);
    }

    public AdaptivePlanDtos.AdaptivePlanResponse getLatestAdaptivePlan(Long childId, Long activityId) {
        AdaptivePlan plan = adaptivePlanRepository.findTopByChildIdAndActivityIdOrderByCreatedAtDesc(childId, activityId)
                .orElseThrow(() -> new ResourceNotFoundException("No adaptive plan found for child " + childId));
        return mapToResponse(plan);
    }

    private AdaptivePlanDtos.AdaptivePlanResponse mapToResponse(AdaptivePlan p) {
        List<Long> stepIds = new ArrayList<>();
        try {
            Long[] arr = objectMapper.readValue(p.getTargetStepIds(), Long[].class);
            stepIds = Arrays.asList(arr);
        } catch (Exception ignored) {}

        return AdaptivePlanDtos.AdaptivePlanResponse.builder()
                .id(p.getId())
                .childId(p.getChild().getId())
                .childName(p.getChild().getFirstName() + " " + p.getChild().getLastName())
                .activityId(p.getActivity().getId())
                .activityTitle(p.getActivity().getTitle())
                .sessionId(p.getSession().getId())
                .currentPromptLevelId(p.getCurrentPromptLevel().getId())
                .currentPromptLevelName(p.getCurrentPromptLevel().getLevelName())
                .targetPromptLevelId(p.getTargetPromptLevel().getId())
                .targetPromptLevelName(p.getTargetPromptLevel().getLevelName())
                .targetStepIds(stepIds)
                .clinicalRationale(p.getClinicalRationale())
                .isConfirmed(p.getIsConfirmed())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
