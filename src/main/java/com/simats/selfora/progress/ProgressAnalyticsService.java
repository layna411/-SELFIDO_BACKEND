package com.simats.selfora.progress;

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

    public ProgressDtos.ProgressSummaryResponse getProgressSummary(Long childId, Long activityId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with ID: " + childId));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        List<TherapySession> sessions = therapySessionRepository.findByChildIdAndActivityIdOrderByStartTimeDesc(childId, activityId);
        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activityId);

        List<PerformanceRecord> allRecords = performanceRecordRepository.findByChildId(childId).stream()
                .filter(r -> r.getStep().getActivity().getId().equals(activityId))
                .collect(Collectors.toList());

        int totalRecords = allRecords.size();
        long independentCount = allRecords.stream()
                .filter(r -> r.getPromptLevel().getId() == 0 && "COMPLETED".equalsIgnoreCase(r.getOutcome()))
                .count();

        double overallIndependencePct = totalRecords > 0 ? ((double) independentCount / totalRecords) * 100.0 : 0.0;

        Map<String, Integer> distribution = new HashMap<>();
        for (PerformanceRecord r : allRecords) {
            String levelName = r.getPromptLevel().getLevelName();
            distribution.put(levelName, distribution.getOrDefault(levelName, 0) + 1);
        }

        // Step-level progress
        List<ProgressDtos.StepProgressItem> stepProgressList = new ArrayList<>();
        for (TaskStep step : steps) {
            List<PerformanceRecord> stepRecords = allRecords.stream()
                    .filter(r -> r.getStep().getId().equals(step.getId()))
                    .collect(Collectors.toList());

            String latestPrompt = stepRecords.isEmpty() ? "Not Practiced" : stepRecords.get(0).getPromptLevel().getLevelName();
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
            List<PerformanceRecord> envRecords = allRecords.stream()
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
            List<PerformanceRecord> sRecords = performanceRecordRepository.findBySessionId(sess.getId());
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
}
