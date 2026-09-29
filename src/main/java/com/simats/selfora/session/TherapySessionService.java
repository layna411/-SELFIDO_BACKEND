package com.simats.selfora.session;

import com.simats.selfora.dto.SessionDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TherapySessionService {

    @Autowired
    private TherapySessionRepository sessionRepository;

    @Autowired
    private PerformanceRecordRepository performanceRecordRepository;

    @Autowired
    private GeneralizationRecordRepository generalizationRecordRepository;

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
    public SessionDtos.SessionResponse startSession(SessionDtos.StartSessionRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        String sessionCode = "SESS-" + System.currentTimeMillis();

        TherapySession session = TherapySession.builder()
                .sessionCode(sessionCode)
                .child(child)
                .conductedByUser(user)
                .activity(activity)
                .sessionType(request.getSessionType() != null ? request.getSessionType() : "CLINIC_THERAPY")
                .environment(request.getEnvironment() != null ? request.getEnvironment() : "CLINIC")
                .build();

        TherapySession saved = sessionRepository.save(session);
        return mapToSessionResponse(saved, Collections.emptyList());
    }

    @Transactional
    public SessionDtos.PerformanceRecordResponse recordPerformance(Long sessionId, SessionDtos.RecordPerformanceRequest request) {
        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        TaskStep step = taskStepRepository.findById(request.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Step not found with ID: " + request.getStepId()));

        PromptLevel promptLevel = promptLevelRepository.findById(request.getPromptLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Prompt level not found with ID: " + request.getPromptLevelId()));

        PerformanceRecord record = PerformanceRecord.builder()
                .session(session)
                .step(step)
                .promptLevel(promptLevel)
                .outcome(request.getOutcome() != null ? request.getOutcome() : "COMPLETED")
                .attempts(request.getAttempts() != null ? request.getAttempts() : 1)
                .durationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0)
                .therapistNote(request.getTherapistNote())
                .caregiverNote(request.getCaregiverNote())
                .build();

        PerformanceRecord savedRecord = performanceRecordRepository.save(record);

        // Record environment in GeneralizationRecord
        String env = request.getEnvironment() != null ? request.getEnvironment() : session.getEnvironment();
        GeneralizationRecord genRecord = GeneralizationRecord.builder()
                .performanceRecord(savedRecord)
                .environment(env)
                .build();
        generalizationRecordRepository.save(genRecord);

        return mapToPerformanceRecordResponse(savedRecord);
    }

    @Transactional
    public SessionDtos.SessionSummaryResponse completeSession(Long sessionId) {
        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        session.setEndTime(LocalDateTime.now());
        if (session.getStartTime() != null) {
            long duration = java.time.Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
            session.setTotalDurationSeconds((int) duration);
        }
        sessionRepository.save(session);

        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(sessionId);
        List<TaskStep> allSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(session.getActivity().getId());

        int totalSteps = allSteps.size();
        int completedSteps = (int) records.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getOutcome())).count();
        int independentSteps = (int) records.stream().filter(r -> r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome())).count();
        int promptedSteps = (int) records.stream().filter(r -> r.getPromptLevel().getId() > 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome())).count();
        int unableSteps = (int) records.stream().filter(r -> "UNABLE".equalsIgnoreCase(r.getOutcome())).count();

        double independencePct = totalSteps > 0 ? ((double) independentSteps / totalSteps) * 100.0 : 0.0;

        Map<String, Integer> distribution = new HashMap<>();
        for (PerformanceRecord r : records) {
            String code = r.getPromptLevel().getLevelCode();
            distribution.put(code, distribution.getOrDefault(code, 0) + 1);
        }

        // Fading Logic Recommendation
        String suggestedNextPrompt = "VERBAL";
        List<Long> suggestedFocusSteps = new ArrayList<>();

        for (PerformanceRecord r : records) {
            if ("UNABLE".equalsIgnoreCase(r.getOutcome()) || r.getPromptLevel().getId() >= 3) {
                suggestedFocusSteps.add(r.getStep().getId());
            }
        }

        if (independencePct >= 80.0) {
            suggestedNextPrompt = "INDEPENDENT";
        } else if (independencePct >= 50.0) {
            suggestedNextPrompt = "VISUAL";
        }

        return SessionDtos.SessionSummaryResponse.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .childName(session.getChild().getFirstName() + " " + session.getChild().getLastName())
                .activityTitle(session.getActivity().getTitle())
                .totalSteps(totalSteps)
                .completedSteps(completedSteps)
                .independentSteps(independentSteps)
                .promptedSteps(promptedSteps)
                .unableSteps(unableSteps)
                .independencePercentage(Math.round(independencePct * 10.0) / 10.0)
                .totalDurationSeconds(session.getTotalDurationSeconds())
                .promptDistribution(distribution)
                .suggestedNextPromptLevel(suggestedNextPrompt)
                .suggestedFocusStepIds(suggestedFocusSteps)
                .build();
    }

    public SessionDtos.SessionResponse getSessionById(Long id) {
        TherapySession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id));
        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(id);
        return mapToSessionResponse(session, records);
    }

    private SessionDtos.SessionResponse mapToSessionResponse(TherapySession session, List<PerformanceRecord> records) {
        return SessionDtos.SessionResponse.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .childId(session.getChild().getId())
                .childName(session.getChild().getFirstName() + " " + session.getChild().getLastName())
                .conductedByUserId(session.getConductedByUser().getId())
                .conductedByUserName(session.getConductedByUser().getFullName())
                .activityId(session.getActivity().getId())
                .activityTitle(session.getActivity().getTitle())
                .sessionType(session.getSessionType())
                .environment(session.getEnvironment())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .totalDurationSeconds(session.getTotalDurationSeconds())
                .summaryNotes(session.getSummaryNotes())
                .performanceRecords(records.stream().map(this::mapToPerformanceRecordResponse).collect(Collectors.toList()))
                .build();
    }

    private SessionDtos.PerformanceRecordResponse mapToPerformanceRecordResponse(PerformanceRecord r) {
        return SessionDtos.PerformanceRecordResponse.builder()
                .id(r.getId())
                .stepId(r.getStep().getId())
                .stepNumber(r.getStep().getStepNumber())
                .stepTitle(r.getStep().getTitle())
                .promptLevelId(r.getPromptLevel().getId())
                .promptLevelCode(r.getPromptLevel().getLevelCode())
                .promptLevelName(r.getPromptLevel().getLevelName())
                .outcome(r.getOutcome())
                .attempts(r.getAttempts())
                .durationSeconds(r.getDurationSeconds())
                .therapistNote(r.getTherapistNote())
                .caregiverNote(r.getCaregiverNote())
                .recordedAt(r.getRecordedAt())
                .build();
    }
}
