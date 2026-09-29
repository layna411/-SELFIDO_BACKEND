package com.simats.selfora.assessment;

import com.simats.selfora.dto.AssessmentDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

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
    public AssessmentDtos.AssessmentResponse createAssessment(AssessmentDtos.CreateAssessmentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        Assessment assessment = Assessment.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .notes(request.getNotes())
                .build();

        Assessment savedAssessment = assessmentRepository.save(assessment);

        List<AssessmentResult> results = request.getStepResults().stream().map(stepReq -> {
            TaskStep step = taskStepRepository.findById(stepReq.getStepId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task step not found: " + stepReq.getStepId()));
            PromptLevel promptLevel = promptLevelRepository.findById(stepReq.getPromptLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prompt level not found: " + stepReq.getPromptLevelId()));

            return AssessmentResult.builder()
                    .assessment(savedAssessment)
                    .step(step)
                    .baselinePromptLevel(promptLevel)
                    .outcome(stepReq.getOutcome() != null ? stepReq.getOutcome() : "SUCCESS")
                    .notes(stepReq.getNotes())
                    .build();
        }).collect(Collectors.toList());

        assessmentResultRepository.saveAll(results);

        return mapToResponse(savedAssessment, results);
    }

    public AssessmentDtos.AssessmentResponse getAssessmentById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + id));
        List<AssessmentResult> results = assessmentResultRepository.findByAssessmentId(id);
        return mapToResponse(assessment, results);
    }

    private AssessmentDtos.AssessmentResponse mapToResponse(Assessment a, List<AssessmentResult> results) {
        return AssessmentDtos.AssessmentResponse.builder()
                .assessmentId(a.getId())
                .childId(a.getChild().getId())
                .childName(a.getChild().getFirstName() + " " + a.getChild().getLastName())
                .therapistId(a.getTherapist().getId())
                .therapistName(a.getTherapist().getFullName())
                .activityId(a.getActivity().getId())
                .activityTitle(a.getActivity().getTitle())
                .assessmentDate(a.getAssessmentDate())
                .notes(a.getNotes())
                .stepResults(results.stream().map(r -> AssessmentDtos.StepResultResponse.builder()
                        .id(r.getId())
                        .stepId(r.getStep().getId())
                        .stepNumber(r.getStep().getStepNumber())
                        .stepTitle(r.getStep().getTitle())
                        .promptLevelId(r.getBaselinePromptLevel().getId())
                        .promptLevelName(r.getBaselinePromptLevel().getLevelName())
                        .outcome(r.getOutcome())
                        .notes(r.getNotes())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
