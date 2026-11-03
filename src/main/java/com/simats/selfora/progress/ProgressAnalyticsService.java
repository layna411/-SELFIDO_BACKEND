package com.simats.selfora.progress;

import com.simats.selfora.child.ChildService;
import com.simats.selfora.dto.ProgressDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgressAnalyticsService {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired
    private PerformanceRecordRepository performanceRecordRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

    @Autowired
    private CaregiverPracticeSessionRepository caregiverPracticeSessionRepository;

    @Autowired
    private ChildService childService;

    public ProgressDtos.ProgressSummaryResponse getProgressSummary(Long childId, Long activityId) {
        childService.validateChildAccessForCurrentUser(childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with ID: " + childId));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        List<TherapySession> sessions = therapySessionRepository.findByChildIdAndActivityIdOrderByStartTimeDesc(childId, activityId);
        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activityId);

        List<PerformanceRecord> allRecords = performanceRecordRepository.findByChildId(childId).stream()
                .filter(r -> r.getStep().getActivity().getId().equals(activityId))
                .collect(Collectors.toList());

        // Exclude Not Assessed steps from denominator
        List<PerformanceRecord> assessedRecords = allRecords.stream()
                .filter(r -> !"NOT_ASSESSED".equalsIgnoreCase(r.getOutcome()) && !"NOT_PRACTICED".equalsIgnoreCase(r.getOutcome()))
                .collect(Collectors.toList());

        int totalAssessed = assessedRecords.size();
        long independentCount = assessedRecords.stream()
                .filter(r -> r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome()))
                .count();

        double overallIndependencePct = totalAssessed > 0 ? ((double) independentCount / totalAssessed) * 100.0 : 0.0;

        Map<String, Integer> distribution = new HashMap<>();
        for (PerformanceRecord r : assessedRecords) {
            String levelName = r.getPromptLevel().getLevelName();
            distribution.put(levelName, distribution.getOrDefault(levelName, 0) + 1);
        }

        // Step-level progress
        List<ProgressDtos.StepProgressItem> stepProgressList = new ArrayList<>();
        for (TaskStep step : steps) {
            List<PerformanceRecord> stepRecords = assessedRecords.stream()
                    .filter(r -> r.getStep().getId().equals(step.getId()))
                    .collect(Collectors.toList());

            String latestPrompt = stepRecords.isEmpty() ? "Not Assessed" : stepRecords.get(0).getPromptLevel().getLevelName();
            long stepIndep = stepRecords.stream()
                    .filter(r -> r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome()))
                    .count();
            double stepPct = !stepRecords.isEmpty() ? ((double) stepIndep / stepRecords.size()) * 100.0 : 0.0;

            stepProgressList.add(ProgressDtos.StepProgressItem.builder()
                    .stepId(step.getId())
                    .stepNumber(step.getStepNumber())
                    .stepTitle(step.getTitle())
                    .currentPromptLevel(latestPrompt)
                    .independencePercentage(Math.round(stepPct * 10.0) / 10.0)
                    .totalAttempts(stepRecords.size())
                    .build());
        }

        // Environment Generalization
        List<ProgressDtos.EnvironmentProgressItem> envProgress = new ArrayList<>();
        Arrays.asList("CLINIC", "HOME", "SCHOOL", "COMMUNITY").forEach(env -> {
            List<PerformanceRecord> envRecords = assessedRecords.stream()
                    .filter(r -> env.equalsIgnoreCase(r.getSession().getEnvironment()))
                    .collect(Collectors.toList());

            if (!envRecords.isEmpty()) {
                long indep = envRecords.stream().filter(r -> r.getPromptLevel().getId() == 0).count();
                double pct = ((double) indep / envRecords.size()) * 100.0;
                String domPrompt = envRecords.get(0).getPromptLevel().getLevelName();

                envProgress.add(ProgressDtos.EnvironmentProgressItem.builder()
                        .environment(env)
                        .independencePercentage(Math.round(pct * 10.0) / 10.0)
                        .dominantPromptLevel(domPrompt)
                        .build());
            }
        });

        // History Trend Points
        List<ProgressDtos.SessionPoint> historyPoints = new ArrayList<>();
        for (TherapySession sess : sessions) {
            List<PerformanceRecord> sRecords = performanceRecordRepository.findBySessionId(sess.getId()).stream()
                    .filter(r -> !"NOT_ASSESSED".equalsIgnoreCase(r.getOutcome()))
                    .collect(Collectors.toList());

            long indep = sRecords.stream().filter(r -> r.getPromptLevel().getId() == 0).count();
            double pct = !sRecords.isEmpty() ? ((double) indep / sRecords.size()) * 100.0 : 0.0;

            historyPoints.add(ProgressDtos.SessionPoint.builder()
                    .sessionId(sess.getId())
                    .sessionDate(sess.getStartTime().toLocalDate().toString())
                    .independencePercentage(Math.round(pct * 10.0) / 10.0)
                    .sessionType(sess.getSessionType())
                    .build());
        }

        return ProgressDtos.ProgressSummaryResponse.builder()
                .childId(child.getId())
                .childName(child.getFirstName() + " " + child.getLastName())
                .activityId(activity.getId())
                .activityTitle(activity.getTitle())
                .overallIndependencePercentage(Math.round(overallIndependencePct * 10.0) / 10.0)
                .totalSessionsCompleted(sessions.size())
                .promptDistribution(distribution)
                .stepProgress(stepProgressList)
                .generalizationProgress(envProgress)
                .historyPoints(historyPoints)
                .build();
    }

    public ProgressDtos.AnalyzeAndAdaptDashboardResponse getAnalyzeAndAdaptDashboard(Long childId, Long activityId) {
        childService.validateChildAccessForCurrentUser(childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with ID: " + childId));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        List<TherapySession> sessions = therapySessionRepository.findByChildIdAndActivityIdOrderByStartTimeDesc(childId, activityId);
        List<Assessment> assessments = assessmentRepository.findByChildIdOrderByAssessmentDateDesc(childId);

        String latestAssessmentDate = assessments.size() > 0 ? assessments.get(0).getAssessmentDate().toString() : "Not Assessed";
        String previousAssessmentDate = assessments.size() > 1 ? assessments.get(1).getAssessmentDate().toString() : "N/A";

        // Current vs Previous Session
        TherapySession currentSession = sessions.size() > 0 ? sessions.get(0) : null;
        TherapySession previousSession = sessions.size() > 1 ? sessions.get(1) : null;

        double currentIndepPct = calculateSessionIndependence(currentSession);
        double previousIndepPct = calculateSessionIndependence(previousSession);

        String trend = currentIndepPct > previousIndepPct ? "IMPROVING" : (currentIndepPct < previousIndepPct ? "REGRESSING" : "STABLE");

        // 4 ADL Activity Cards (Dressing, Eating, Grooming, Shoes & Socks)
        List<ProgressDtos.ActivityPerformanceCardDto> activityCards = buildActivityPerformanceCards(childId);

        // Difficult Steps
        List<ProgressDtos.DifficultStepDto> difficultSteps = findDifficultSteps(childId, activityId);

        // Consistently Independent Steps
        List<ProgressDtos.IndependentStepDto> independentSteps = findIndependentSteps(childId, activityId);

        // Session Comparison
        ProgressDtos.SessionComparisonDto sessionComparison = compareSessions(currentSession, previousSession);

        // Factual Clinical Observations
        List<String> factualObservations = generateFactualObservations(childId, activityId, sessions);

        // Rule-Based Recommendations
        List<ProgressDtos.RecommendationDto> recommendations = generateRuleBasedRecommendations(childId, activityId, sessions, difficultSteps, independentSteps);

        // Caregiver Home Practice Trend (Distinct from clinical score)
        List<CaregiverPracticeSession> cgSessions = caregiverPracticeSessionRepository.findByChildIdOrderByPracticeDateDesc(childId);
        int totalSubmissions = cgSessions.size();
        long completedActivities = cgSessions.stream()
                .filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus()) || "COMPLETED".equalsIgnoreCase(s.getStatus()) || "SUBMITTED".equalsIgnoreCase(s.getStatus()))
                .count();
        String summary = totalSubmissions > 0
                ? "Child completed " + totalSubmissions + " home practice sessions recorded by caregiver."
                : "No home practice sessions recorded yet by caregiver.";
        String freq = totalSubmissions > 0 ? totalSubmissions + " recorded submissions" : "None";
        ProgressDtos.CaregiverProgressSummaryDto caregiverSummary = new ProgressDtos.CaregiverProgressSummaryDto(
                totalSubmissions, (int) completedActivities, summary, freq, true
        );

        return ProgressDtos.AnalyzeAndAdaptDashboardResponse.builder()
                .childId(child.getId())
                .childName(child.getFirstName() + " " + child.getLastName())
                .selectedActivityId(activity.getId())
                .selectedActivityTitle(activity.getTitle())
                .totalRecordedSessions(sessions.size())
                .latestAssessmentDate(latestAssessmentDate)
                .previousAssessmentDate(previousAssessmentDate)
                .currentIndependencePercentage(Math.round(currentIndepPct * 10.0) / 10.0)
                .previousIndependencePercentage(Math.round(previousIndepPct * 10.0) / 10.0)
                .overallTrend(trend)
                .activityCards(activityCards)
                .difficultSteps(difficultSteps)
                .independentSteps(independentSteps)
                .sessionComparison(sessionComparison)
                .factualObservations(factualObservations)
                .recommendations(recommendations)
                .caregiverProgressSummary(caregiverSummary)
                .build();
    }

    private double calculateSessionIndependence(TherapySession session) {
        if (session == null) return 0.0;
        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(session.getId()).stream()
                .filter(r -> !"NOT_ASSESSED".equalsIgnoreCase(r.getOutcome()))
                .collect(Collectors.toList());

        if (records.isEmpty()) return 0.0;
        long independentCount = records.stream()
                .filter(r -> r.getPromptLevel() != null && r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome()))
                .count();
        return ((double) independentCount / records.size()) * 100.0;
    }

    private List<ProgressDtos.ActivityPerformanceCardDto> buildActivityPerformanceCards(Long childId) {
        List<ProgressDtos.ActivityPerformanceCardDto> cards = new ArrayList<>();

        Map<String, String> categoryMap = new LinkedHashMap<>();
        categoryMap.put("Dressing", "Dressing");
        categoryMap.put("Eating", "Eating");
        categoryMap.put("Grooming", "Grooming");
        categoryMap.put("Shoes & Socks", "Shoes & Socks");

        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            String cat = entry.getKey();
            List<TherapySession> sList = therapySessionRepository.findByChildId(childId).stream()
                    .filter(s -> s.getActivity() != null && s.getActivity().getTitle().toLowerCase().contains(cat.toLowerCase()))
                    .collect(Collectors.toList());

            double curPct = 0.0;
            double prevPct = 0.0;
            String trendStr = "STABLE";

            if (!sList.isEmpty()) {
                curPct = calculateSessionIndependence(sList.get(0));
                if (sList.size() > 1) {
                    prevPct = calculateSessionIndependence(sList.get(1));
                    trendStr = curPct > prevPct ? "IMPROVING" : (curPct < prevPct ? "REGRESSING" : "STABLE");
                } else {
                    prevPct = 0.0;
                    trendStr = curPct > 0 ? "IMPROVING" : "STABLE";
                }
            }

            cards.add(new ProgressDtos.ActivityPerformanceCardDto(
                    cat, Math.round(curPct * 10.0) / 10.0, Math.round(prevPct * 10.0) / 10.0, trendStr, sList.size()
            ));
        }

        return cards;
    }

    private List<ProgressDtos.DifficultStepDto> findDifficultSteps(Long childId, Long activityId) {
        List<ProgressDtos.DifficultStepDto> list = new ArrayList<>();
        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activityId);

        for (TaskStep step : steps) {
            List<PerformanceRecord> records = performanceRecordRepository.findByChildId(childId).stream()
                    .filter(r -> r.getStep().getId().equals(step.getId()))
                    .collect(Collectors.toList());

            int physicalCount = (int) records.stream().filter(r -> r.getPromptLevel() != null && r.getPromptLevel().getId() >= 5).count();
            int verbalCount = (int) records.stream().filter(r -> r.getPromptLevel() != null && r.getPromptLevel().getId() == 3).count();
            int unableCount = (int) records.stream().filter(r -> "UNABLE".equalsIgnoreCase(r.getOutcome())).count();

            if (physicalCount > 1 || unableCount > 0 || verbalCount >= 3) {
                String mainAssistance = physicalCount > 0 ? "Physical Assistance (L5/L6)" : (verbalCount > 0 ? "Verbal Prompt (L3)" : "Unable");
                String desc = "Step " + step.getStepNumber() + " (" + step.getTitle() + ") frequently required " + mainAssistance.toLowerCase() + " across recent sessions.";

                list.add(new ProgressDtos.DifficultStepDto(
                        step.getId(), step.getStepNumber(), step.getTitle(), step.getActivity().getTitle(),
                        mainAssistance, physicalCount, verbalCount, unableCount, desc
                ));
            }
        }

        return list;
    }

    private List<ProgressDtos.IndependentStepDto> findIndependentSteps(Long childId, Long activityId) {
        List<ProgressDtos.IndependentStepDto> list = new ArrayList<>();
        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activityId);

        for (TaskStep step : steps) {
            List<PerformanceRecord> records = performanceRecordRepository.findByChildId(childId).stream()
                    .filter(r -> r.getStep().getId().equals(step.getId()))
                    .collect(Collectors.toList());

            long indepCount = records.stream()
                    .filter(r -> r.getPromptLevel() != null && r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome()))
                    .count();

            if (indepCount >= 2) {
                list.add(new ProgressDtos.IndependentStepDto(
                        step.getId(), step.getStepNumber(), step.getTitle(), step.getActivity().getTitle(),
                        (int) indepCount, "Ready for Therapist Prompt Fading Review & Generalization Assessment"
                ));
            }
        }

        return list;
    }

    private ProgressDtos.SessionComparisonDto compareSessions(TherapySession current, TherapySession previous) {
        if (current == null && previous == null) {
            return null;
        }
        String curDate = current != null && current.getStartTime() != null ? current.getStartTime().toLocalDate().toString() : "N/A";
        String prevDate = previous != null && previous.getStartTime() != null ? previous.getStartTime().toLocalDate().toString() : "N/A";

        double curPct = calculateSessionIndependence(current);
        double prevPct = calculateSessionIndependence(previous);

        List<String> improved = new ArrayList<>();
        List<String> regressed = new ArrayList<>();
        List<String> unchanged = new ArrayList<>();

        if (current != null && previous != null) {
            Map<Long, Integer> prevStepPrompts = performanceRecordRepository.findBySessionId(previous.getId()).stream()
                    .filter(r -> r.getPromptLevel() != null)
                    .collect(Collectors.toMap(r -> r.getStep().getId(), r -> r.getPromptLevel().getId(), (a, b) -> a));

            List<PerformanceRecord> curRecords = performanceRecordRepository.findBySessionId(current.getId());
            for (PerformanceRecord cr : curRecords) {
                if (cr.getPromptLevel() == null) continue;
                Long sId = cr.getStep().getId();
                String stepLabel = "Step " + cr.getStep().getStepNumber() + ": " + cr.getStep().getTitle();
                if (prevStepPrompts.containsKey(sId)) {
                    int prevLvl = prevStepPrompts.get(sId);
                    int curLvl = cr.getPromptLevel().getId();
                    if (curLvl < prevLvl) {
                        improved.add(stepLabel);
                    } else if (curLvl > prevLvl) {
                        regressed.add(stepLabel);
                    } else {
                        unchanged.add(stepLabel);
                    }
                }
            }
        }

        return new ProgressDtos.SessionComparisonDto(curDate, prevDate, Math.round(curPct * 10.0) / 10.0, Math.round(prevPct * 10.0) / 10.0, improved, regressed, unchanged);
    }

    private List<String> generateFactualObservations(Long childId, Long activityId, List<TherapySession> sessions) {
        List<String> observations = new ArrayList<>();
        int sessionCount = sessions.size();
        if (sessionCount == 0) {
            observations.add("No clinical therapy sessions recorded yet for this activity.");
            return observations;
        }
        observations.add("Child completed " + sessionCount + " clinical therapy session" + (sessionCount > 1 ? "s" : "") + " for this ADL activity.");

        List<PerformanceRecord> records = performanceRecordRepository.findByChildId(childId).stream()
                .filter(r -> r.getStep().getActivity().getId().equals(activityId))
                .collect(Collectors.toList());

        long unableCount = records.stream().filter(r -> "UNABLE".equalsIgnoreCase(r.getOutcome())).count();
        if (unableCount == 0 && !records.isEmpty()) {
            observations.add("No 'Unable' outcomes recorded across assessed sessions for this activity.");
        } else if (unableCount > 0) {
            observations.add(unableCount + " task step attempt(s) recorded as 'Unable' across sessions.");
        }

        long indepCount = records.stream().filter(r -> r.getPromptLevel() != null && r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome())).count();
        if (indepCount > 0) {
            observations.add(indepCount + " step repetition(s) completed independently (Level 0) without assistance.");
        }
        return observations;
    }

    private List<ProgressDtos.RecommendationDto> generateRuleBasedRecommendations(
            Long childId, Long activityId, List<TherapySession> sessions,
            List<ProgressDtos.DifficultStepDto> difficultSteps, List<ProgressDtos.IndependentStepDto> independentSteps
    ) {
        List<ProgressDtos.RecommendationDto> recommendations = new ArrayList<>();

        for (ProgressDtos.DifficultStepDto d : difficultSteps) {
            if (d.getPhysicalAssistanceCount() > 0 || d.getMainAssistanceLevel().contains("Physical")) {
                recommendations.add(new ProgressDtos.RecommendationDto(
                        "Repeated physical assistance (L5/L6) recorded on " + d.getStepTitle(),
                        "Recent assessed sessions",
                        d.getActivityTitle(),
                        d.getStepTitle(),
                        "Review task step breakdown and teaching approach with physical therapist."
                ));
            } else if (d.getUnableCount() > 0) {
                recommendations.add(new ProgressDtos.RecommendationDto(
                        "Repeated 'Unable' outcome recorded on " + d.getStepTitle(),
                        "Recent assessed sessions",
                        d.getActivityTitle(),
                        d.getStepTitle(),
                        "Review activity suitability, sensory barriers, and adaptation options with therapist."
                ));
            } else if (d.getVerbalPromptCount() >= 3) {
                recommendations.add(new ProgressDtos.RecommendationDto(
                        "Frequent verbal prompts required on " + d.getStepTitle(),
                        "Recent assessed sessions",
                        d.getActivityTitle(),
                        d.getStepTitle(),
                        "Consider introducing visual cues to transition from verbal to visual prompting."
                ));
            }
        }

        for (ProgressDtos.IndependentStepDto ind : independentSteps) {
            if (ind.getConsecutiveIndependentSessions() >= 2) {
                recommendations.add(new ProgressDtos.RecommendationDto(
                        "Consistent independent performance (" + ind.getConsecutiveIndependentSessions() + " sessions) on " + ind.getStepTitle(),
                        "Last " + ind.getConsecutiveIndependentSessions() + " sessions",
                        ind.getActivityTitle(),
                        ind.getStepTitle(),
                        "Consider therapist review of readiness for prompt fading."
                ));
            }
        }

        return recommendations;
    }
}
