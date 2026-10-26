package com.simats.selfora.child;

import com.simats.selfora.dto.ChildModeDtos.*;
import com.simats.selfora.entity.*;
import com.simats.selfora.entity.HomeProgram;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChildModeService {

    @Autowired
    private ChildService childService;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private HomeProgramRepository homeProgramRepository;

    @Autowired
    private ChildActivityProgressRepository childActivityProgressRepository;

    @Autowired
    private ChildRewardRepository childRewardRepository;

    @Autowired
    private ChildModePreferenceRepository childModePreferenceRepository;

    @Transactional
    public ChildDashboardResponse getChildDashboard(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + childId));

        ChildDashboardResponse response = new ChildDashboardResponse();
        response.setChildId(child.getId());
        response.setChildName(child.getFirstName());
        response.setGender(child.getGender());
        response.setAvatarUrl(child.getAvatarUrl());

        // Preferences
        ChildModePreference pref = childModePreferenceRepository.findByChildId(childId)
                .orElseGet(() -> {
                    ChildModePreference newPref = new ChildModePreference(child, child.getFirstName());
                    return childModePreferenceRepository.save(newPref);
                });
        ChildModePreferenceDto prefDto = new ChildModePreferenceDto();
        prefDto.setPreferredName(pref.getPreferredName() != null ? pref.getPreferredName() : child.getFirstName());
        prefDto.setAudioEnabled(pref.getAudioEnabled());
        prefDto.setAnimationEnabled(pref.getAnimationEnabled());
        prefDto.setReducedMotion(pref.getReducedMotion());
        prefDto.setLanguage(pref.getLanguage());
        prefDto.setRewardSounds(pref.getRewardSounds());
        response.setPreferences(prefDto);
        response.setPreferredName(prefDto.getPreferredName());

        // Assigned Home Programs
        List<HomeProgram> activePrograms = homeProgramRepository.findByChildId(childId)
                .stream()
                .filter(hp -> Boolean.TRUE.equals(hp.getIsActive()))
                .collect(Collectors.toList());

        List<Long> assignedActivityIds = activePrograms.stream()
                .map(hp -> hp.getActivity().getId())
                .distinct()
                .collect(Collectors.toList());

        // All activities
        List<Activity> allActivities = activityRepository.findAll();
        List<ChildActivityProgress> progressList = childActivityProgressRepository.findByChildId(childId);

        int totalStars = progressList.stream().mapToInt(p -> p.getStarsEarned() != null ? p.getStarsEarned() : 0).sum();
        int completedCount = (int) progressList.stream().filter(p -> Boolean.TRUE.equals(p.getIsCompleted())).count();

        response.setTotalStars(totalStars);
        response.setCompletedCount(completedCount);

        List<ChildActivitySummaryDto> assignedSummaries = new ArrayList<>();
        List<ChildActivitySummaryDto> allSummaries = new ArrayList<>();

        for (Activity act : allActivities) {
            boolean isAssigned = assignedActivityIds.contains(act.getId());
            Optional<ChildActivityProgress> pOpt = progressList.stream()
                    .filter(p -> p.getActivity().getId().equals(act.getId()))
                    .findFirst();

            ChildActivitySummaryDto sum = new ChildActivitySummaryDto();
            sum.setActivityId(act.getId());
            sum.setTitle(act.getTitle());
            sum.setDescription(act.getDescription());
            sum.setIconUrl(act.getIconUrl());
            sum.setCategoryName(act.getCategory() != null ? act.getCategory().getName() : "General");
            
            List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(act.getId());
            sum.setTotalSteps(steps.size());

            if (pOpt.isPresent()) {
                ChildActivityProgress cap = pOpt.get();
                sum.setCompletedSteps(cap.getTotalStepsCompleted() != null ? cap.getTotalStepsCompleted() : 0);
                sum.setCompleted(Boolean.TRUE.equals(cap.getIsCompleted()));
                sum.setStarsEarned(cap.getStarsEarned() != null ? cap.getStarsEarned() : 0);
            } else {
                sum.setCompletedSteps(0);
                sum.setCompleted(false);
                sum.setStarsEarned(0);
            }

            sum.setAssigned(isAssigned);
            allSummaries.add(sum);

            if (isAssigned) {
                assignedSummaries.add(sum);
            }
        }

        response.setAssignedActivities(assignedSummaries);
        response.setAllActivities(allSummaries);

        // Last unfinished activity for "Continue Learning"
        Optional<ChildActivityProgress> lastUnfinished = childActivityProgressRepository
                .findFirstByChildIdAndIsCompletedFalseOrderByLastPracticedAtDesc(childId);
        if (lastUnfinished.isPresent()) {
            Long actId = lastUnfinished.get().getActivity().getId();
            allSummaries.stream()
                    .filter(s -> s.getActivityId().equals(actId))
                    .findFirst()
                    .ifPresent(response::setContinueActivity);
        } else if (!assignedSummaries.isEmpty()) {
            response.setContinueActivity(assignedSummaries.get(0));
        }

        // Earned Rewards
        List<ChildReward> rewards = childRewardRepository.findByChildIdOrderByEarnedAtDesc(childId);
        List<ChildRewardDto> rewardDtos = rewards.stream().map(r -> {
            ChildRewardDto dto = new ChildRewardDto();
            dto.setId(r.getId());
            dto.setBadgeType(r.getBadgeType());
            dto.setBadgeTitle(r.getBadgeTitle());
            dto.setBadgeIcon(r.getBadgeIcon());
            dto.setEarnedAt(r.getEarnedAt());
            return dto;
        }).collect(Collectors.toList());

        response.setEarnedRewards(rewardDtos);

        return response;
    }

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ChildModeService self;

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void ensureProgressRecordExists(Long childId, Long activityId) {
        childActivityProgressRepository.insertIgnoreProgress(childId, activityId);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void awardBadgeIfEligible(Child child, String type, String title, String icon) {
        childRewardRepository.insertIgnoreReward(child.getId(), type, title, icon);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public ChildActivityProgress recordStepProgress(RecordStepProgressRequest request) {
        childService.validateChildAccessForCurrentUser(request.getChildId());

        // Step 1: Ensure row exists in DB via committed micro-transaction BEFORE main transaction snapshot
        self.ensureProgressRecordExists(request.getChildId(), request.getActivityId());

        // Step 2: Perform update in new transactional snapshot that sees the committed record
        return self.doUpdateStepProgress(request);
    }

    @Transactional
    public ChildActivityProgress doUpdateStepProgress(RecordStepProgressRequest request) {
        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + request.getChildId()));
        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + request.getActivityId()));

        ChildActivityProgress progress = childActivityProgressRepository
                .findByChildIdAndActivityId(request.getChildId(), request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress record missing for child " + request.getChildId()));

        boolean alreadyCompleted = Boolean.TRUE.equals(progress.getIsCompleted());

        if (request.getStepNumber() != null) {
            progress.setLastStepNumber(request.getStepNumber());
            if (request.getStepNumber() > progress.getTotalStepsCompleted()) {
                progress.setTotalStepsCompleted(request.getStepNumber());
            }
        }

        if (Boolean.TRUE.equals(request.getIsActivityCompleted())) {
            progress.setIsCompleted(true);
            if (!alreadyCompleted) {
                progress.setStarsEarned(5);

                // Award badge if not already awarded in isolated transaction
                self.awardBadgeIfEligible(child, "COMPLETION_STAR", "Star Performer", "ic_star_gold");
            }
        }

        return childActivityProgressRepository.saveAndFlush(progress);
    }

    public List<ChildRewardDto> getChildRewards(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);

        return childRewardRepository.findByChildIdOrderByEarnedAtDesc(childId)
                .stream()
                .map(r -> {
                    ChildRewardDto dto = new ChildRewardDto();
                    dto.setId(r.getId());
                    dto.setBadgeType(r.getBadgeType());
                    dto.setBadgeTitle(r.getBadgeTitle());
                    dto.setBadgeIcon(r.getBadgeIcon());
                    dto.setEarnedAt(r.getEarnedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ChildModePreferenceDto getChildPreferences(Long childId) {
        childService.validateChildAccessForCurrentUser(childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + childId));

        ChildModePreference pref = childModePreferenceRepository.findByChildId(childId)
                .orElseGet(() -> {
                    ChildModePreference newPref = new ChildModePreference(child, child.getFirstName());
                    return childModePreferenceRepository.save(newPref);
                });

        ChildModePreferenceDto dto = new ChildModePreferenceDto();
        dto.setPreferredName(pref.getPreferredName() != null ? pref.getPreferredName() : child.getFirstName());
        dto.setAudioEnabled(pref.getAudioEnabled());
        dto.setAnimationEnabled(pref.getAnimationEnabled());
        dto.setReducedMotion(pref.getReducedMotion());
        dto.setLanguage(pref.getLanguage());
        dto.setRewardSounds(pref.getRewardSounds());

        return dto;
    }

    public ChildModePreferenceDto updateChildPreferences(Long childId, ChildModePreferenceDto dto) {
        childService.validateChildAccessForCurrentUser(childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + childId));

        ChildModePreference pref = childModePreferenceRepository.findByChildId(childId)
                .orElseGet(() -> new ChildModePreference(child, child.getFirstName()));

        if (dto.getPreferredName() != null) pref.setPreferredName(dto.getPreferredName());
        if (dto.getAudioEnabled() != null) pref.setAudioEnabled(dto.getAudioEnabled());
        if (dto.getAnimationEnabled() != null) pref.setAnimationEnabled(dto.getAnimationEnabled());
        if (dto.getReducedMotion() != null) pref.setReducedMotion(dto.getReducedMotion());
        if (dto.getLanguage() != null) pref.setLanguage(dto.getLanguage());
        if (dto.getRewardSounds() != null) pref.setRewardSounds(dto.getRewardSounds());

        ChildModePreference saved = childModePreferenceRepository.save(pref);

        ChildModePreferenceDto res = new ChildModePreferenceDto();
        res.setPreferredName(saved.getPreferredName());
        res.setAudioEnabled(saved.getAudioEnabled());
        res.setAnimationEnabled(saved.getAnimationEnabled());
        res.setReducedMotion(saved.getReducedMotion());
        res.setLanguage(saved.getLanguage());
        res.setRewardSounds(saved.getRewardSounds());

        return res;
    }
}
