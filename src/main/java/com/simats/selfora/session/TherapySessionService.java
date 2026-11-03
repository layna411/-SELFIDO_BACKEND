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
    private UserRepository userRepository;

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
    private com.simats.selfora.child.ChildService childService;

    @Transactional
    public SessionDtos.SessionResponse startSession(SessionDtos.StartSessionRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        childService.validateChildAccessForCurrentUser(request.getChildId());

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
        if (request == null) {
            throw new IllegalArgumentException("RecordPerformanceRequest cannot be null");
        }
        if (request.getStepId() == null) {
            throw new IllegalArgumentException("Step ID is required");
        }

        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        childService.validateChildAccessForCurrentUser(session.getChild().getId());

        TaskStep step = taskStepRepository.findById(request.getStepId())
                .orElseThrow(() -> new ResourceNotFoundException("Step not found with ID: " + request.getStepId()));

        String outcome = request.getOutcome();
        Integer levelId = request.getPromptLevelId();

        if (levelId != null && (levelId < 0 || levelId > 8)) {
            throw new IllegalArgumentException("Invalid prompt level ID: " + levelId + ". Supported prompt levels are 0 to 8.");
        }

        if ("UNABLE".equalsIgnoreCase(outcome)) {
            if (levelId == null) levelId = 7;
        } else if ("NOT_ASSESSED".equalsIgnoreCase(outcome)) {
            if (levelId == null) levelId = 8;
        } else if (levelId != null && levelId == 7) {
            outcome = "UNABLE";
        } else if (levelId != null && levelId == 8) {
            outcome = "NOT_ASSESSED";
        } else {
            if (levelId == null) {
                throw new IllegalArgumentException("Prompt level ID is required for assessed steps");
            }
            if (outcome == null || outcome.isBlank()) {
                outcome = "COMPLETED";
            }
        }

        final String finalOutcome = outcome;
        final int targetLevelId = levelId;

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

        // Find existing record for this session & step to update, or create a new one
        PerformanceRecord record = performanceRecordRepository.findBySessionIdAndStepId(sessionId, step.getId())
                .orElse(null);

        if (record == null) {
            record = PerformanceRecord.builder()
                    .session(session)
                    .step(step)
                    .promptLevel(promptLevel)
                    .outcome(finalOutcome)
                    .attempts(request.getAttempts() != null ? request.getAttempts() : 1)
                    .durationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0)
                    .therapistNote(request.getTherapistNote())
                    .caregiverNote(request.getCaregiverNote())
                    .build();
        } else {
            record.setPromptLevel(promptLevel);
            record.setOutcome(finalOutcome);
            if (request.getAttempts() != null) record.setAttempts(request.getAttempts());
            if (request.getDurationSeconds() != null) record.setDurationSeconds(request.getDurationSeconds());
            if (request.getTherapistNote() != null) record.setTherapistNote(request.getTherapistNote());
            if (request.getCaregiverNote() != null) record.setCaregiverNote(request.getCaregiverNote());
        }

        PerformanceRecord savedRecord = performanceRecordRepository.save(record);

        // Record environment in GeneralizationRecord
        String env = request.getEnvironment() != null ? request.getEnvironment() : session.getEnvironment();
        List<GeneralizationRecord> genRecords = generalizationRecordRepository.findByPerformanceRecordId(savedRecord.getId());
        GeneralizationRecord genRecord;
        if (!genRecords.isEmpty()) {
            genRecord = genRecords.get(0);
            genRecord.setEnvironment(env);
        } else {
            genRecord = GeneralizationRecord.builder()
                    .performanceRecord(savedRecord)
                    .environment(env)
                    .build();
        }
        generalizationRecordRepository.save(genRecord);

        return mapToPerformanceRecordResponse(savedRecord);
    }

    @Transactional
    public SessionDtos.SessionSummaryResponse completeSession(Long sessionId) {
        TherapySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        childService.validateChildAccessForCurrentUser(session.getChild().getId());

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
        int promptedSteps = (int) records.stream().filter(r -> r.getPromptLevel().getId() > 0 && r.getPromptLevel().getId() <= 6 && "COMPLETED".equalsIgnoreCase(r.getOutcome())).count();
        int unableSteps = (int) records.stream().filter(r -> "UNABLE".equalsIgnoreCase(r.getOutcome()) || r.getPromptLevel().getId() == 7).count();
        int totalAssessedSteps = (int) records.stream().filter(r -> !"NOT_ASSESSED".equalsIgnoreCase(r.getOutcome()) && r.getPromptLevel().getId() != 8).count();

        double independencePct = totalAssessedSteps > 0 ? ((double) independentSteps / totalAssessedSteps) * 100.0 : 0.0;

        Map<String, Integer> distribution = new HashMap<>();
        for (PerformanceRecord r : records) {
            String code = r.getPromptLevel().getLevelCode();
            distribution.put(code, distribution.getOrDefault(code, 0) + 1);
        }

        // Fading Logic Recommendation
        String suggestedNextPrompt = "VERBAL";
        List<Long> suggestedFocusSteps = new ArrayList<>();

        for (PerformanceRecord r : records) {
            if ("UNABLE".equalsIgnoreCase(r.getOutcome()) || r.getPromptLevel().getId() == 7 || r.getPromptLevel().getId() >= 3) {
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
        childService.validateChildAccessForCurrentUser(session.getChild().getId());
        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(id);
        return mapToSessionResponse(session, records);
    }

    public List<SessionDtos.SessionResponse> getSessionsForChild(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);
        List<TherapySession> sessions = sessionRepository.findByChildIdOrderByStartTimeDesc(childId);
        return sessions.stream()
                .map(s -> mapToSessionResponse(s, performanceRecordRepository.findBySessionId(s.getId())))
                .collect(Collectors.toList());
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
