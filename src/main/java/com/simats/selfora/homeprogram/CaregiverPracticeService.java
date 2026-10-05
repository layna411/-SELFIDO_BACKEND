package com.simats.selfora.homeprogram;

import com.simats.selfora.child.ChildService;
import com.simats.selfora.dto.CaregiverPracticeDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaregiverPracticeService {

    @Autowired
    private CaregiverPracticeSessionRepository practiceSessionRepository;

    @Autowired
    private CaregiverPracticeStepResultRepository stepResultRepository;

    @Autowired
    private HomeProgramRepository homeProgramRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChildService childService;

    public CaregiverPracticeDtos.CaregiverPracticeSessionResponse submitPractice(CaregiverPracticeDtos.SubmitPracticeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User caregiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver user not found"));

        childService.validateChildAccessForCurrentUser(request.getChildId());

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        HomeProgram program = homeProgramRepository.findById(request.getHomeProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Home program not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        // Check for duplicate submission on same date
        boolean exists = practiceSessionRepository.existsByChildIdAndHomeProgramIdAndPracticeDate(
                child.getId(), program.getId(), request.getPracticeDate()
        );
        if (exists) {
            // Retrieve existing or update
            List<CaregiverPracticeSession> existingList = practiceSessionRepository.findByChildIdOrderByPracticeDateDesc(child.getId());
            if (!existingList.isEmpty()) {
                return mapToResponse(existingList.get(0));
            }
        }

        CaregiverPracticeSession session = CaregiverPracticeSession.builder()
                .child(child)
                .caregiver(caregiver)
                .homeProgram(program)
                .activity(activity)
                .practiceDate(request.getPracticeDate())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 15)
                .caregiverNotes(request.getCaregiverNotes())
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .build();

        CaregiverPracticeSession saved = practiceSessionRepository.save(session);

        if (request.getStepResults() != null) {
            for (CaregiverPracticeDtos.StepResultItemDto dto : request.getStepResults()) {
                TaskStep step = taskStepRepository.findById(dto.getStepId()).orElse(null);
                if (step != null) {
                    CaregiverPracticeStepResult stepResult = CaregiverPracticeStepResult.builder()
                            .practiceSession(saved)
                            .step(step)
                            .outcome(dto.getOutcome() != null ? dto.getOutcome() : "INDEPENDENT")
                            .observation(dto.getObservation())
                            .build();
                    stepResultRepository.save(stepResult);
                }
            }
        }

        // Notify therapist
        if (program.getTherapist() != null) {
            notificationService.sendNotification(
                    program.getTherapist(),
                    "New Home Practice Submission Received",
                    caregiver.getFirstName() + " submitted home practice for " + child.getFirstName() + " " + child.getLastName(),
                    "PRACTICE_SUBMITTED",
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    public CaregiverPracticeDtos.CaregiverPracticeSessionResponse reviewPracticeSession(Long sessionId, CaregiverPracticeDtos.TherapistReviewRequest request) {
        CaregiverPracticeSession session = practiceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice session not found"));

        childService.validateChildAccessForCurrentUser(session.getChild().getId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist user not found"));

        session.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "REVIEWED");
        session.setReviewingTherapist(therapist);
        session.setReviewedAt(LocalDateTime.now());
        session.setTherapistFeedback(request.getFeedbackText());
        session.setFlaggedForFollowup(request.getFlaggedForFollowup() != null ? request.getFlaggedForFollowup() : false);

        CaregiverPracticeSession saved = practiceSessionRepository.save(session);

        // Notify caregiver
        if (session.getCaregiver() != null) {
            notificationService.sendNotification(
                    session.getCaregiver(),
                    "Therapist Feedback Available",
                    "Dr. " + therapist.getLastName() + " reviewed " + session.getChild().getFirstName() + "'s home practice.",
                    "FEEDBACK_AVAILABLE",
                    saved.getId()
            );
        }

        return mapToResponse(saved);
    }

    public List<CaregiverPracticeDtos.CaregiverPracticeSessionResponse> getPendingReviewsForTherapist() {
        return practiceSessionRepository.findByStatusOrderBySubmittedAtDesc("SUBMITTED").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CaregiverPracticeDtos.CaregiverPracticeSessionResponse> getPracticeHistoryForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);
        return practiceSessionRepository.findByChildIdOrderByPracticeDateDesc(childId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CaregiverPracticeDtos.CaregiverPracticeSessionResponse mapToResponse(CaregiverPracticeSession s) {
        List<CaregiverPracticeStepResult> stepResults = stepResultRepository.findByPracticeSessionId(s.getId());
        List<CaregiverPracticeDtos.StepResultItemDto> stepDtos = stepResults.stream()
                .map(sr -> new CaregiverPracticeDtos.StepResultItemDto(
                        sr.getStep().getId(),
                        sr.getStep().getStepNumber(),
                        sr.getStep().getTitle(),
                        sr.getOutcome(),
                        sr.getObservation()
                ))
                .collect(Collectors.toList());

        String therapistName = s.getReviewingTherapist() != null
                ? s.getReviewingTherapist().getFirstName() + " " + s.getReviewingTherapist().getLastName()
                : null;

        String progTitle = s.getHomeProgram() != null ? s.getHomeProgram().getTitle() : "Home Practice Programme";

        return CaregiverPracticeDtos.CaregiverPracticeSessionResponse.builder()
                .id(s.getId())
                .homeProgramId(s.getHomeProgram().getId())
                .homeProgramTitle(progTitle)
                .childId(s.getChild().getId())
                .childName(s.getChild().getFirstName() + " " + s.getChild().getLastName())
                .caregiverId(s.getCaregiver().getId())
                .caregiverName(s.getCaregiver().getFirstName() + " " + s.getCaregiver().getLastName())
                .activityId(s.getActivity().getId())
                .activityTitle(s.getActivity().getTitle())
                .practiceDate(s.getPracticeDate())
                .durationMinutes(s.getDurationMinutes())
                .caregiverNotes(s.getCaregiverNotes())
                .status(s.getStatus())
                .submittedAt(s.getSubmittedAt())
                .reviewingTherapistName(therapistName)
                .reviewedAt(s.getReviewedAt())
                .therapistFeedback(s.getTherapistFeedback())
                .flaggedForFollowup(s.getFlaggedForFollowup())
                .stepResults(stepDtos)
                .build();
    }
}
