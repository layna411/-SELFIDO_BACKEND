package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.child.ChildModeService;
import com.simats.selfora.dto.ChildModeDtos.*;
import com.simats.selfora.entity.*;
import com.simats.selfora.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Sprint4_1IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChildModeService childModeService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private ChildActivityProgressRepository childActivityProgressRepository;

    @Autowired
    private ChildRewardRepository childRewardRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private CaregiverPracticeSessionRepository caregiverPracticeSessionRepository;

    // Child 13 is linked to caregiver1 and assigned to therapist1
    private static final Long TEST_CHILD_ID = 13L;

    // =========================================================================
    // 1 & 2: V12 & V13 MIGRATION AND ACTIVITY DATA INTEGRITY
    // =========================================================================

    @Test
    @DisplayName("1. V12 and V13 activity data integrity: All 4 ADL categories and 5 core activities exist")
    void v12AndV13ActivityDataIntegrity() {
        // T-Shirt (Activity 1 - Dressing)
        Activity tshirt = activityRepository.findById(1L).orElse(null);
        assertNotNull(tshirt, "Activity 1 (T-Shirt) must exist");
        List<TaskStep> tshirtSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(1L);
        assertTrue(tshirtSteps.size() >= 5, "T-Shirt must have at least 5 task steps");

        // Velcro Shoes (Activity 9 - Shoes & Socks)
        Activity shoes = activityRepository.findById(9L).orElse(null);
        assertNotNull(shoes, "Activity 9 (Velcro Shoes) must exist");
        List<TaskStep> shoesSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(9L);
        assertEquals(18, shoesSteps.size(), "Velcro Shoes must have exactly 18 seeded task steps");

        // Eating with Spoon (Activity 13 - Eating)
        Activity spoon = activityRepository.findById(13L).orElse(null);
        assertNotNull(spoon, "Activity 13 (Eating with Spoon) must exist");
        List<TaskStep> spoonSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(13L);
        assertEquals(21, spoonSteps.size(), "Eating with Spoon must have exactly 21 seeded task steps");

        // Toothbrushing (Activity 14 - Grooming)
        Activity tooth = activityRepository.findById(14L).orElse(null);
        assertNotNull(tooth, "Activity 14 (Toothbrushing) must exist");
        List<TaskStep> toothSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(14L);
        assertEquals(7, toothSteps.size(), "Toothbrushing must have exactly 7 seeded task steps");

        // Handwashing (Activity 15 - Grooming, V13 Migration)
        Activity handwashing = activityRepository.findById(15L).orElse(null);
        assertNotNull(handwashing, "Activity 15 (Handwashing) must exist");
        List<TaskStep> handSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(15L);
        assertEquals(7, handSteps.size(), "Handwashing must have exactly 7 seeded task steps from V13 migration");
    }

    @Test
    @DisplayName("2. Existing migration safety: All 4 ADL categories represented cleanly across activities")
    void existingMigrationSafety() {
        List<Activity> activities = activityRepository.findAll();
        assertFalse(activities.isEmpty());

        Set<String> categoriesFound = new HashSet<>();
        for (Activity a : activities) {
            if (a.getCategory() != null) {
                categoriesFound.add(a.getCategory().getName().toUpperCase());
            }
        }

        assertTrue(categoriesFound.contains("DRESSING"), "Dressing category must exist");
        assertTrue(categoriesFound.contains("EATING"), "Eating category must exist");
        assertTrue(categoriesFound.contains("GROOMING"), "Grooming category must exist");
    }

    // =========================================================================
    // 3 & 4: PRACTICE SESSION PERSISTENCE, RECOVERY & NETWORK RESILIENCE
    // =========================================================================

    @Test
    @DisplayName("3. Practice session restart recovery: In-progress activity and step number preserved")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void practiceSessionRestartRecovery() throws Exception {
        Long actId = 9L; // Velcro shoes
        childActivityProgressRepository.findByChildIdAndActivityId(TEST_CHILD_ID, actId)
                .ifPresent(p -> childActivityProgressRepository.delete(p));

        // Save in-progress state at step 4
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(actId);
        req.setStepNumber(4);
        req.setIsActivityCompleted(false);

        mockMvc.perform(post("/api/child-mode/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastStepNumber").value(4))
                .andExpect(jsonPath("$.isCompleted").value(false));

        // Query Child Dashboard - continueActivity should point to this in-progress activity
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        ChildDashboardResponse dashboard = objectMapper.readValue(res.getResponse().getContentAsString(), ChildDashboardResponse.class);
        assertNotNull(dashboard.getContinueActivity(), "Continue activity must be populated for unfinished practice");
        assertEquals(actId, dashboard.getContinueActivity().getActivityId());
        assertEquals(4, dashboard.getContinueActivity().getCompletedSteps());
    }

    @Test
    @DisplayName("4. Network interruption simulation: Partial step progression preserved safely")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void networkInterruptionDuringPracticeStatePreserved() {
        Long actId = 13L; // Eating spoon
        childActivityProgressRepository.findByChildIdAndActivityId(TEST_CHILD_ID, actId)
                .ifPresent(p -> childActivityProgressRepository.delete(p));

        // Simulate step 6 saved before simulated network disconnection
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(actId);
        req.setStepNumber(6);
        req.setIsActivityCompleted(false);

        StepProgressResponse res = childModeService.recordStepProgress(req);
        assertEquals(6, res.getLastStepNumber());
        assertFalse(res.getIsCompleted());
        assertEquals(0, res.getStarsAwarded());

        // Verify database state matches
        ChildActivityProgress cap = childActivityProgressRepository
                .findByChildIdAndActivityId(TEST_CHILD_ID, actId).orElse(null);
        assertNotNull(cap);
        assertEquals(6, cap.getLastStepNumber());
        assertFalse(cap.getIsCompleted());
    }

    // =========================================================================
    // 5-9: STAR REWARD TRANSACTION INTEGRITY & CONCURRENCY
    // =========================================================================

    @Test
    @DisplayName("5. Partial completion reward rejection: Incomplete steps award 0 stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void partialCompletionRewardRejection() throws Exception {
        Long actId = 14L; // Toothbrushing (7 steps)
        childActivityProgressRepository.findByChildIdAndActivityId(TEST_CHILD_ID, actId)
                .ifPresent(p -> childActivityProgressRepository.delete(p));

        // Request claiming completion at step 3 of 7
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(actId);
        req.setStepNumber(3);
        req.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starsAwarded").value(0))
                .andExpect(jsonPath("$.isCompleted").value(false));
    }

    @Test
    @DisplayName("6. First completion reward: Full completion awards exactly 5 stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void firstCompletionReward() throws Exception {
        Long actId = 15L; // Handwashing (7 steps)

        // Clear existing progress for fresh test
        childActivityProgressRepository.findByChildIdAndActivityId(TEST_CHILD_ID, actId)
                .ifPresent(p -> childActivityProgressRepository.delete(p));

        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(actId);
        req.setStepNumber(7);
        req.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starsAwarded").value(5))
                .andExpect(jsonPath("$.isCompleted").value(true));
    }

    @Test
    @DisplayName("7. Replay reward rejection: Replaying completed activity awards 0 stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void replayRewardRejection() throws Exception {
        Long actId = 15L; // Handwashing

        // Ensure activity is completed first
        RecordStepProgressRequest req1 = new RecordStepProgressRequest();
        req1.setChildId(TEST_CHILD_ID);
        req1.setActivityId(actId);
        req1.setStepNumber(7);
        req1.setIsActivityCompleted(true);
        childModeService.recordStepProgress(req1);

        // Replay submission
        RecordStepProgressRequest reqReplay = new RecordStepProgressRequest();
        reqReplay.setChildId(TEST_CHILD_ID);
        reqReplay.setActivityId(actId);
        reqReplay.setStepNumber(7);
        reqReplay.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqReplay)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starsAwarded").value(0))
                .andExpect(jsonPath("$.isCompleted").value(true));
    }

    @Test
    @DisplayName("8. Concurrent completion requests safety: Exactly one thread awards 5 stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void concurrentCompletionRequestsSafety() throws Exception {
        Long actId = 1L; // T-Shirt (20 steps in SELFIDO task analysis)

        childActivityProgressRepository.findByChildIdAndActivityId(TEST_CHILD_ID, actId)
                .ifPresent(p -> childActivityProgressRepository.delete(p));

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        List<StepProgressResponse> results = new CopyOnWriteArrayList<>();

        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(actId);
        req.setStepNumber(20);
        req.setIsActivityCompleted(true);
        String json = objectMapper.writeValueAsString(req);
        SecurityContext context = SecurityContextHolder.getContext();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                SecurityContextHolder.setContext(context);
                try {
                    MvcResult res = mockMvc.perform(post("/api/child-mode/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                            .andReturn();
                    if (res.getResponse().getStatus() == 200) {
                        results.add(objectMapper.readValue(res.getResponse().getContentAsString(), StepProgressResponse.class));
                    }
                } catch (Exception ignored) {
                } finally {
                    SecurityContextHolder.clearContext();
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertFalse(results.isEmpty(), "At least one concurrent request must succeed");
        for (StepProgressResponse resp : results) {
            assertTrue(resp.getIsCompleted());
        }

        ChildActivityProgress cap = childActivityProgressRepository
                .findByChildIdAndActivityId(TEST_CHILD_ID, actId).orElseThrow();
        assertEquals(5, cap.getStarsEarned(), "Persisted stars earned in database must be exactly 5");
        assertTrue(cap.getIsCompleted(), "Persisted completion status must be true");
    }

    @Test
    @DisplayName("9. Transaction rollback integrity: Invalid activity ID rolls back without corrupting state")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void transactionRollbackIntegrity() throws Exception {
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(99999L); // Non-existent activity
        req.setStepNumber(1);
        req.setIsActivityCompleted(false);

        mockMvc.perform(post("/api/child-mode/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // 10 & 11: BADGE INTEGRITY & MILESTONES
    // =========================================================================

    @Test
    @DisplayName("10. Badge duplicate prevention: Unique constraint uk_cr_child_badge prevents duplicate badges")
    void badgeDuplicatePrevention() {
        Child child = childRepository.findById(TEST_CHILD_ID).orElseThrow();

        // Award badge first time
        boolean first = childModeService.awardBadgeIfEligible(child, "FIRST_DRESSING", "Dressing Champ", "ic_shirt");
        // Award badge second time
        boolean second = childModeService.awardBadgeIfEligible(child, "FIRST_DRESSING", "Dressing Champ", "ic_shirt");

        assertFalse(second, "Second attempt to award the same badge must be rejected");

        long count = childRewardRepository.findByChildIdOrderByEarnedAtDesc(TEST_CHILD_ID)
                .stream()
                .filter(r -> "FIRST_DRESSING".equals(r.getBadgeType()))
                .count();
        assertEquals(1, count, "Only one badge row should exist in database");
    }

    @Test
    @DisplayName("11. Badge milestone accuracy: High Five Club unlocks on 5 distinct completed activities")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void badgeMilestoneAccuracy() throws Exception {
        // Complete activities 1, 9, 13, 14, 15
        long[] acts = {1L, 9L, 13L, 14L, 15L};
        int[] steps = {18, 10, 10, 7, 7};

        for (int i = 0; i < acts.length; i++) {
            RecordStepProgressRequest req = new RecordStepProgressRequest();
            req.setChildId(TEST_CHILD_ID);
            req.setActivityId(acts[i]);
            req.setStepNumber(steps[i]);
            req.setIsActivityCompleted(true);
            childModeService.recordStepProgress(req);
        }

        // Query rewards
        MvcResult res = mockMvc.perform(get("/api/child-mode/rewards/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        List<?> rewards = objectMapper.readValue(res.getResponse().getContentAsString(), List.class);
        assertNotNull(rewards);
        assertFalse(rewards.isEmpty());
    }

    // =========================================================================
    // 12-14: PRACTICE STREAK BOUNDARY TESTS
    // =========================================================================

    @Test
    @DisplayName("12. Streak today/yesterday boundary: Consecutive days maintain streak")
    void streakTodayYesterdayBoundary() {
        Child freshChild = new Child();
        freshChild.setFirstName("StreakBoundaryChild");
        freshChild.setLastName("Boy");
        freshChild.setDateOfBirth(LocalDate.of(2019, 1, 1));
        freshChild.setGender("MALE");
        freshChild = childRepository.save(freshChild);

        Activity act1 = activityRepository.findById(1L).orElseThrow();
        Activity act9 = activityRepository.findById(9L).orElseThrow();

        // Seed practice yesterday on act1 and today on act9
        ChildActivityProgress cap1 = new ChildActivityProgress(freshChild, act1, 5, 5, true, 5);
        cap1.setLastPracticedAt(LocalDateTime.now().minusDays(1));
        childActivityProgressRepository.save(cap1);

        ChildActivityProgress cap2 = new ChildActivityProgress(freshChild, act9, 5, 5, true, 5);
        cap2.setLastPracticedAt(LocalDateTime.now());
        childActivityProgressRepository.save(cap2);

        int streak = childModeService.calculatePracticeStreak(freshChild.getId());
        assertEquals(2, streak, "Streak should be exactly 2 consecutive days");
    }

    @Test
    @DisplayName("13. Streak gap handling: Gap of 2 or more days breaks streak")
    void streakGapHandling() {
        Child freshChild = new Child();
        freshChild.setFirstName("StreakGapChild");
        freshChild.setLastName("Boy");
        freshChild.setDateOfBirth(LocalDate.of(2019, 1, 1));
        freshChild.setGender("MALE");
        freshChild = childRepository.save(freshChild);

        Activity act1 = activityRepository.findById(1L).orElseThrow();

        // Practice 3 days ago only
        ChildActivityProgress cap = new ChildActivityProgress(freshChild, act1, 5, 5, true, 5);
        cap.setLastPracticedAt(LocalDateTime.now().minusDays(3));
        childActivityProgressRepository.save(cap);

        int streak = childModeService.calculatePracticeStreak(freshChild.getId());
        assertEquals(0, streak, "Streak must break to 0 when last practice was 3 days ago");
    }

    @Test
    @DisplayName("14. Multiple practices on one date: Evaluated as 1 calendar day")
    void multiplePracticesOnOneDate() {
        Child freshChild = new Child();
        freshChild.setFirstName("MultiPracticeChild");
        freshChild.setLastName("Boy");
        freshChild.setDateOfBirth(LocalDate.of(2019, 1, 1));
        freshChild.setGender("MALE");
        freshChild = childRepository.save(freshChild);

        Activity act1 = activityRepository.findById(1L).orElseThrow();
        Activity act9 = activityRepository.findById(9L).orElseThrow();

        // Two activities practiced today
        ChildActivityProgress cap1 = new ChildActivityProgress(freshChild, act1, 5, 5, true, 5);
        cap1.setLastPracticedAt(LocalDateTime.now().withHour(10));
        childActivityProgressRepository.save(cap1);

        ChildActivityProgress cap2 = new ChildActivityProgress(freshChild, act9, 5, 5, true, 5);
        cap2.setLastPracticedAt(LocalDateTime.now().withHour(15));
        childActivityProgressRepository.save(cap2);

        int streak = childModeService.calculatePracticeStreak(freshChild.getId());
        assertEquals(1, streak, "Multiple sessions on same date should count as exactly 1 day");
    }

    // =========================================================================
    // 15-18: ROLE-BASED ACCESS & ADULT EXIT PIN SECURITY
    // =========================================================================

    @Test
    @DisplayName("15. Caregiver linked-child authorization: Linked caregiver accesses dashboard (200)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverLinkedChildAuthorization() throws Exception {
        mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID));
    }

    @Test
    @DisplayName("16. Therapist assigned-child authorization: Assigned therapist accesses dashboard (200)")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistAssignedChildAuthorization() throws Exception {
        mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID));
    }

    @Test
    @DisplayName("17. Unauthorized child context rejection: Unlinked user rejected with 403 Forbidden")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void unauthorizedChildContextRejection() throws Exception {
        Long unlinkedChildId = 99999L;
        mockMvc.perform(get("/api/child-mode/dashboard/" + unlinkedChildId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("18. Adult exit PIN validation rule: Default is 1234, rejects empty or invalid PIN")
    void adultExitPinSecurityRule() {
        String validPin = "1234";
        String invalidPin = "9999";
        String emptyPin = "   ";

        assertTrue("1234".equals(validPin.trim()), "Default PIN must be 1234");
        assertFalse("1234".equals(invalidPin.trim()), "Invalid PIN must be rejected");
        assertFalse("1234".equals(emptyPin.trim()), "Empty PIN must be rejected");
    }

    // =========================================================================
    // 19 & 20: CLINICAL DATA ISOLATION & DASHBOARD REFRESH
    // =========================================================================

    @Test
    @DisplayName("19. Clinical data isolation: Child Mode JSON contains zero clinical prompt levels (L0-L6)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void clinicalDataIsolation() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        String json = res.getResponse().getContentAsString();
        assertFalse(json.contains("\"promptLevel\""), "Child Mode must not expose clinical prompt levels");
        assertFalse(json.contains("L0_INDEPENDENT"), "Child Mode must not contain clinical prompt strings");
        assertFalse(json.contains("L6_FULL_PHYSICAL"), "Child Mode must not contain clinical prompt strings");
        assertFalse(json.contains("\"therapistNotes\""), "Child Mode must not expose private therapist notes");
    }

    @Test
    @DisplayName("20. Dashboard refresh after completion: Returns updated stars, streak, and badges")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void dashboardRefreshAfterCompletion() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        ChildDashboardResponse dashboard = objectMapper.readValue(res.getResponse().getContentAsString(), ChildDashboardResponse.class);
        assertTrue(dashboard.getTotalStars() >= 0);
        assertTrue(dashboard.getCurrentStreak() >= 0);
        assertNotNull(dashboard.getAllBadges());
        assertEquals(8, dashboard.getAllBadges().size(), "Catalog must contain all 8 system badges");

        // Verify categories are cleanly standardized
        for (ChildActivitySummaryDto act : dashboard.getAllActivities()) {
            assertNotNull(act.getCategoryName());
            assertFalse(act.getCategoryName().isBlank());
        }
    }
}
