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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private CaregiverPracticeSessionRepository caregiverPracticeSessionRepository;

    public static class BadgeDefinition {
        private final String type;
        private final String title;
        private final String icon;
        private final String description;

        public BadgeDefinition(String type, String title, String icon, String description) {
            this.type = type;
            this.title = title;
            this.icon = icon;
            this.description = description;
        }

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }

    private static final List<BadgeDefinition> SYSTEM_BADGES = Arrays.asList(
        new BadgeDefinition("FIRST_COMPLETION", "First Step Star", "ic_star_gold", "Completed your very first ADL activity!"),
        new BadgeDefinition("FIRST_DRESSING", "Dressing Champ", "ic_shirt", "Mastered a dressing activity!"),
        new BadgeDefinition("FIRST_EATING", "Mealtime Master", "ic_spoon", "Mastered an eating activity!"),
        new BadgeDefinition("FIRST_GROOMING", "Grooming Hero", "ic_brush", "Mastered a grooming activity!"),
        new BadgeDefinition("FIRST_SHOES", "Shoe Wizard", "ic_shoes", "Mastered putting on shoes and socks!"),
        new BadgeDefinition("FIVE_COMPLETED", "High Five Club", "ic_medal_bronze", "Completed 5 ADL activities!"),
        new BadgeDefinition("TEN_COMPLETED", "Super Champion", "ic_trophy_gold", "Completed 10 ADL activities!"),
        new BadgeDefinition("PRACTICE_STREAK_3", "3-Day Streak", "ic_fire", "Practised 3 days in a row!")
    );

    public static final java.time.ZoneId CLINICAL_ZONE = java.time.ZoneId.of("Asia/Kolkata");

    public int calculatePracticeStreak(Long childId) {
        Set<LocalDate> dates = new TreeSet<>(Collections.reverseOrder());

        try {
            List<CaregiverPracticeSession> sessions = caregiverPracticeSessionRepository.findByChildIdOrderByPracticeDateDesc(childId);
            for (CaregiverPracticeSession s : sessions) {
                if (s.getPracticeDate() != null) {
                    dates.add(s.getPracticeDate());
                }
            }
        } catch (Exception ignored) {}

        try {
            List<ChildActivityProgress> progress = childActivityProgressRepository.findByChildId(childId);
            for (ChildActivityProgress p : progress) {
                if (p.getLastPracticedAt() != null) {
                    dates.add(p.getLastPracticedAt().toLocalDate());
                }
            }
        } catch (Exception ignored) {}

        if (dates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDate current = null;
        if (dates.contains(today)) {
            current = today;
        } else if (dates.contains(yesterday)) {
            current = yesterday;
        } else {
            return 0;
        }

        int streak = 0;
        while (dates.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }

        return streak;
    }

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

        // Practice streak from authentic persisted dates
        int streak = calculatePracticeStreak(childId);
        response.setCurrentStreak(streak);

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
            
            // Standardize category display for Child Mode (Dressing, Eating, Grooming, Shoes & Socks)
            String displayCategory = "General";
            if (act.getCategory() != null) {
                long catId = act.getCategory().getId() != null ? act.getCategory().getId() : 0L;
                String actTitle = act.getTitle() != null ? act.getTitle().toLowerCase() : "";
                if (catId == 5L || actTitle.contains("shoe") || actTitle.contains("sock")) {
                    displayCategory = "Shoes & Socks";
                } else if (act.getCategory().getName().equalsIgnoreCase("DRESSING")) {
                    displayCategory = "Dressing";
                } else if (act.getCategory().getName().equalsIgnoreCase("EATING")) {
                    displayCategory = "Eating";
                } else if (act.getCategory().getName().equalsIgnoreCase("GROOMING")) {
                    displayCategory = "Grooming";
                } else {
                    displayCategory = act.getCategory().getName();
                }
            }
            sum.setCategoryName(displayCategory);
            
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

        // Earned Rewards and All Badges catalog
        List<ChildReward> rewards = childRewardRepository.findByChildIdOrderByEarnedAtDesc(childId);
        Map<String, ChildReward> earnedMap = rewards.stream()
                .collect(Collectors.toMap(ChildReward::getBadgeType, r -> r, (r1, r2) -> r1));

        List<ChildRewardDto> rewardDtos = rewards.stream().map(r -> {
            ChildRewardDto dto = new ChildRewardDto();
            dto.setId(r.getId());
            dto.setBadgeType(r.getBadgeType());
            dto.setBadgeTitle(r.getBadgeTitle());
            dto.setBadgeIcon(r.getBadgeIcon());
            dto.setUnlocked(true);
            dto.setEarnedAt(r.getEarnedAt());
            return dto;
        }).collect(Collectors.toList());
        response.setEarnedRewards(rewardDtos);

        List<ChildRewardDto> allBadges = SYSTEM_BADGES.stream().map(b -> {
            ChildReward earned = earnedMap.get(b.getType());
            ChildRewardDto dto = new ChildRewardDto();
            dto.setBadgeType(b.getType());
            dto.setBadgeTitle(b.getTitle());
            dto.setBadgeIcon(b.getIcon());
            dto.setDescription(b.getDescription());
            if (earned != null) {
                dto.setId(earned.getId());
                dto.setUnlocked(true);
                dto.setEarnedAt(earned.getEarnedAt());
            } else {
                dto.setUnlocked(false);
                dto.setEarnedAt(null);
            }
            return dto;
        }).collect(Collectors.toList());
        response.setAllBadges(allBadges);

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
    public boolean awardBadgeIfEligible(Child child, String type, String title, String icon) {
        if (childRewardRepository.existsByChildIdAndBadgeType(child.getId(), type)) {
            return false;
        }
        childRewardRepository.insertIgnoreReward(child.getId(), type, title, icon);
        return true;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public StepProgressResponse recordStepProgress(RecordStepProgressRequest request) {
        childService.validateChildAccessForCurrentUser(request.getChildId());

        // Step 1: Ensure row exists in DB via committed micro-transaction BEFORE main transaction snapshot
        self.ensureProgressRecordExists(request.getChildId(), request.getActivityId());

        // Step 2: Perform update in new transactional snapshot that sees the committed record
        return self.doUpdateStepProgress(request);
    }

    @Transactional
    public StepProgressResponse doUpdateStepProgress(RecordStepProgressRequest request) {
        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + request.getChildId()));
        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + request.getActivityId()));

        ChildActivityProgress progress = childActivityProgressRepository
                .findByChildIdAndActivityId(request.getChildId(), request.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress record missing for child " + request.getChildId()));

        boolean alreadyCompleted = Boolean.TRUE.equals(progress.getIsCompleted());
        int starsAwarded = 0;
        String newlyUnlockedBadge = null;
        String newlyUnlockedBadgeIcon = null;

        // Total steps required
        int totalSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activity.getId()).size();
        if (totalSteps == 0) totalSteps = 1;

        if (request.getStepNumber() != null) {
            progress.setLastStepNumber(request.getStepNumber());
            if (request.getStepNumber() > progress.getTotalStepsCompleted()) {
                progress.setTotalStepsCompleted(request.getStepNumber());
            }
        }
        progress.setLastPracticedAt(LocalDateTime.now());

        // Check completion criteria
        boolean meetsCompletion = (request.getStepNumber() != null && request.getStepNumber() >= totalSteps)
                || (progress.getTotalStepsCompleted() != null && progress.getTotalStepsCompleted() >= totalSteps);

        if (Boolean.TRUE.equals(request.getIsActivityCompleted())) {
            if (meetsCompletion) {
                progress.setIsCompleted(true);
                if (!alreadyCompleted) {
                    progress.setStarsEarned(5);
                    starsAwarded = 5;

                    // 1. First completion badge
                    if (self.awardBadgeIfEligible(child, "FIRST_COMPLETION", "First Step Star", "ic_star_gold")) {
                        newlyUnlockedBadge = "First Step Star";
                        newlyUnlockedBadgeIcon = "ic_star_gold";
                    }

                    // 2. Category badge
                    String categoryName = activity.getCategory() != null ? activity.getCategory().getName().toUpperCase() : "";
                    String activityTitle = activity.getTitle() != null ? activity.getTitle().toUpperCase() : "";
                    if (categoryName.contains("EATING") || activityTitle.contains("EAT") || activityTitle.contains("SPOON")) {
                        if (self.awardBadgeIfEligible(child, "FIRST_EATING", "Mealtime Master", "ic_spoon")) {
                            newlyUnlockedBadge = "Mealtime Master";
                            newlyUnlockedBadgeIcon = "ic_spoon";
                        }
                    } else if (categoryName.contains("GROOMING") || activityTitle.contains("TOOTH") || activityTitle.contains("BRUSH") || activityTitle.contains("HAND")) {
                        if (self.awardBadgeIfEligible(child, "FIRST_GROOMING", "Grooming Hero", "ic_brush")) {
                            newlyUnlockedBadge = "Grooming Hero";
                            newlyUnlockedBadgeIcon = "ic_brush";
                        }
                    } else if (categoryName.contains("COMMON") && (activityTitle.contains("SHOE") || activityTitle.contains("SOCK")) || activityTitle.contains("SHOE") || activityTitle.contains("SOCK")) {
                        if (self.awardBadgeIfEligible(child, "FIRST_SHOES", "Shoe Wizard", "ic_shoes")) {
                            newlyUnlockedBadge = "Shoe Wizard";
                            newlyUnlockedBadgeIcon = "ic_shoes";
                        }
                    } else if (categoryName.contains("DRESSING")) {
                        if (self.awardBadgeIfEligible(child, "FIRST_DRESSING", "Dressing Champ", "ic_shirt")) {
                            newlyUnlockedBadge = "Dressing Champ";
                            newlyUnlockedBadgeIcon = "ic_shirt";
                        }
                    }

                    // 3. Count milestones (completed activities for this child)
                    long completedCount = childActivityProgressRepository.findByChildId(child.getId()).stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsCompleted()))
                            .count();
                    if (completedCount >= 5) {
                        if (self.awardBadgeIfEligible(child, "FIVE_COMPLETED", "High Five Club", "ic_medal_bronze")) {
                            newlyUnlockedBadge = "High Five Club";
                            newlyUnlockedBadgeIcon = "ic_medal_bronze";
                        }
                    }
                    if (completedCount >= 10) {
                        if (self.awardBadgeIfEligible(child, "TEN_COMPLETED", "Super Champion", "ic_trophy_gold")) {
                            newlyUnlockedBadge = "Super Champion";
                            newlyUnlockedBadgeIcon = "ic_trophy_gold";
                        }
                    }

                    // 4. Streak milestone
                    int streak = calculatePracticeStreak(child.getId());
                    if (streak >= 3) {
                        if (self.awardBadgeIfEligible(child, "PRACTICE_STREAK_3", "3-Day Streak", "ic_fire")) {
                            newlyUnlockedBadge = "3-Day Streak";
                            newlyUnlockedBadgeIcon = "ic_fire";
                        }
                    }
                } else {
                    // Replay of already completed activity: no new stars awarded
                    starsAwarded = 0;
                }
            } else {
                // Incomplete activity cannot receive completion reward
                progress.setIsCompleted(false);
                starsAwarded = 0;
            }
        }

        ChildActivityProgress saved = childActivityProgressRepository.saveAndFlush(progress);

        // Recalculate total star balance
        int totalStars = childActivityProgressRepository.findByChildId(child.getId()).stream()
                .mapToInt(p -> p.getStarsEarned() != null ? p.getStarsEarned() : 0)
                .sum();

        StepProgressResponse res = new StepProgressResponse();
        res.setId(saved.getId());
        res.setChildId(child.getId());
        res.setActivityId(activity.getId());
        res.setActivityTitle(activity.getTitle());
        res.setLastStepNumber(saved.getLastStepNumber());
        res.setTotalStepsCompleted(saved.getTotalStepsCompleted());
        res.setIsCompleted(saved.getIsCompleted());
        res.setStarsEarned(saved.getStarsEarned());
        res.setStarsAwarded(starsAwarded);
        res.setTotalStars(totalStars);
        res.setNewlyUnlockedBadge(newlyUnlockedBadge);
        res.setNewlyUnlockedBadgeIcon(newlyUnlockedBadgeIcon);
        res.setLastPracticedAt(saved.getLastPracticedAt());

        return res;
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
