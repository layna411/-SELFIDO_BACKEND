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
    private UserRepository userRepository;

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
    private com.simats.selfora.child.ChildService childService;

    @Transactional
    public AssessmentDtos.AssessmentResponse createAssessment(AssessmentDtos.CreateAssessmentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        childService.validateChildAccessForCurrentUser(request.getChildId());

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
            
            Integer levelId = stepReq.getPromptLevelId();
            String outcome = stepReq.getOutcome();

            if ("UNABLE".equalsIgnoreCase(outcome)) {
                if (levelId == null) levelId = 7;
            } else if ("NOT_ASSESSED".equalsIgnoreCase(outcome)) {
                if (levelId == null) levelId = 8;
            } else if (levelId != null && levelId == 7) {
                outcome = "UNABLE";
            } else if (levelId != null && levelId == 8) {
                outcome = "NOT_ASSESSED";
            } else {
                if (levelId == null) levelId = 0;
                if (outcome == null || outcome.isBlank()) outcome = "COMPLETED";
            }

            final int targetLevelId = levelId;
            final String finalOutcome = outcome;

            PromptLevel promptLevel = promptLevelRepository.findById(targetLevelId)
                    .orElseGet(() -> {
                        if (targetLevelId == 7) {
                            return promptLevelRepository.save(PromptLevel.builder().id(7).levelCode("UNABLE").levelName("Unable").hierarchyOrder(7).build());
                        } else if (targetLevelId == 8) {
                            return promptLevelRepository.save(PromptLevel.builder().id(8).levelCode("NOT_ASSESSED").levelName("Not Assessed").hierarchyOrder(8).build());
                        } else {
                            return promptLevelRepository.findByLevelCode("INDEPENDENT")
                                    .orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(0).levelCode("INDEPENDENT").levelName("Independent").hierarchyOrder(0).build()));
                        }
                    });

            return AssessmentResult.builder()
                    .assessment(savedAssessment)
                    .step(step)
                    .baselinePromptLevel(promptLevel)
                    .outcome(finalOutcome)
                    .notes(stepReq.getNotes())
                    .build();
        }).collect(Collectors.toList());

        assessmentResultRepository.saveAll(results);

        return mapToResponse(savedAssessment, results);
    }

    public AssessmentDtos.AssessmentResponse getAssessmentById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + id));
        childService.validateChildAccessForCurrentUser(assessment.getChild().getId());
        List<AssessmentResult> results = assessmentResultRepository.findByAssessmentId(id);
        return mapToResponse(assessment, results);
    }

    public List<AssessmentDtos.AssessmentResponse> getAssessmentsForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);
        List<Assessment> assessments = assessmentRepository.findByChildIdOrderByAssessmentDateDesc(childId);
        return assessments.stream()
                .map(a -> mapToResponse(a, assessmentResultRepository.findByAssessmentId(a.getId())))
                .collect(Collectors.toList());
    }

    public AssessmentDtos.AssessmentResponse getLatestAssessmentForChild(Long childId, Long activityId) {
        childService.validateChildAccessForCurrentUser(childId);
        Assessment assessment = (activityId != null)
                ? assessmentRepository.findTopByChildIdAndActivityIdOrderByAssessmentDateDesc(childId, activityId)
                        .orElseThrow(() -> new ResourceNotFoundException("No assessment found for child ID " + childId + " and activity ID " + activityId))
                : assessmentRepository.findByChildIdOrderByAssessmentDateDesc(childId).stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No assessment found for child ID " + childId));
        List<AssessmentResult> results = assessmentResultRepository.findByAssessmentId(assessment.getId());
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
