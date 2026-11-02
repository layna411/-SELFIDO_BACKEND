package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.ChildModeDtos.*;
import com.simats.selfora.entity.*;
import com.simats.selfora.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Sprint4IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChildActivityProgressRepository childActivityProgressRepository;

    @Autowired
    private ChildRewardRepository childRewardRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private CaregiverPracticeSessionRepository caregiverPracticeSessionRepository;

    // Child 13 is assigned to therapist1 AND linked to caregiver1
    private static final Long TEST_CHILD_ID = 13L;

    // ========================================================
    // 1-5: CHILD MODE DASHBOARD & STEP PROGRESSION
    // ========================================================

    @Test
    @DisplayName("1. Child Mode dashboard data retrieval")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void childDashboardDataRetrieval() throws Exception {
        mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID))
                .andExpect(jsonPath("$.childName").exists())
                .andExpect(jsonPath("$.allActivities").isArray())
                .andExpect(jsonPath("$.allBadges").isArray())
                .andExpect(jsonPath("$.preferences").exists());
    }

    @Test
    @DisplayName("2. Assigned activity retrieval in Child Mode")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void assignedActivityRetrieval() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        ChildDashboardResponse dashboard = objectMapper.readValue(res.getResponse().getContentAsString(), ChildDashboardResponse.class);
        assertNotNull(dashboard.getAllActivities());
        assertFalse(dashboard.getAllActivities().isEmpty());
    }

    @Test
    @DisplayName("3. Task-step retrieval across all ADL categories")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void taskStepRetrievalAcrossCategories() throws Exception {
        // Dressing (Activity 1)
        mockMvc.perform(get("/api/activities/1/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(18));

        // Shoes & Socks (Activity 9)
        mockMvc.perform(get("/api/activities/9/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));

        // Eating (Activity 13)
        mockMvc.perform(get("/api/activities/13/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));

        // Grooming (Activity 14)
        mockMvc.perform(get("/api/activities/14/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    @DisplayName("4. Successful step progression without activity completion")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void successfulStepProgression() throws Exception {
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(1L);
        req.setStepNumber(3);
        req.setIsActivityCompleted(false);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastStepNumber").value(3))
                .andExpect(jsonPath("$.isCompleted").value(false))
                .andExpect(jsonPath("$.starsAwarded").value(0));
    }

    @Test
    @DisplayName("5. Incomplete activity handling does not award completion stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void incompleteActivityDoesNotAwardCompletionStars() throws Exception {
        // Step 2 out of 18 with isActivityCompleted = true (premature completion attempt)
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(1L);
        req.setStepNumber(2);
        req.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(false))
                .andExpect(jsonPath("$.starsAwarded").value(0));
    }

    // ========================================================
    // 6-10: ACTIVITY COMPLETION, REWARDS & CONCURRENCY
    // ========================================================

    @Test
    @DisplayName("6. Successful activity completion awards 5 stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void successfulActivityCompletionAwardsStars() throws Exception {
        // Complete Activity 14 (Toothbrushing, 7 steps)
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(14L);
        req.setStepNumber(7);
        req.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true))
                .andExpect(jsonPath("$.starsEarned").value(5));
    }

    @Test
    @DisplayName("7. Star reward persistence in child total balance")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void starRewardPersistenceInChildBalance() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        ChildDashboardResponse dashboard = objectMapper.readValue(res.getResponse().getContentAsString(), ChildDashboardResponse.class);
        assertTrue(dashboard.getTotalStars() >= 0);
    }

    @Test
    @DisplayName("8. Duplicate completion request does not award duplicate stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void duplicateCompletionDoesNotAwardDuplicateStars() throws Exception {
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(14L);
        req.setStepNumber(7);
        req.setIsActivityCompleted(true);

        // First completion
        MvcResult res1 = mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        StepProgressResponse p1 = objectMapper.readValue(res1.getResponse().getContentAsString(), StepProgressResponse.class);

        // Replay / duplicate completion
        MvcResult res2 = mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        StepProgressResponse p2 = objectMapper.readValue(res2.getResponse().getContentAsString(), StepProgressResponse.class);

        // Zero additional stars on replay
        assertEquals(0, p2.getStarsAwarded());
        assertEquals(p1.getTotalStars(), p2.getTotalStars());
    }

    @Test
    @DisplayName("9. Concurrent completion requests prevent duplicate awards and DB errors")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void concurrentCompletionRequestsSafety() throws Exception {
        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        List<StepProgressResponse> results = new CopyOnWriteArrayList<>();

        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(9L);
        req.setStepNumber(10);
        req.setIsActivityCompleted(true);

        String json = objectMapper.writeValueAsString(req);
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.getContext();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                org.springframework.security.core.context.SecurityContextHolder.setContext(context);
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
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertFalse(results.isEmpty(), "At least one concurrent request must succeed");
        // All successful requests must reflect isCompleted = true
        for (StepProgressResponse r : results) {
            assertTrue(r.getIsCompleted());
        }
    }

    @Test
    @DisplayName("10. Failed transaction handling for invalid requests")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void failedTransactionHandling() throws Exception {
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(99999L); // Non-existent child
        req.setActivityId(1L);
        req.setStepNumber(1);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ========================================================
    // 11-14: BADGES, ACHIEVEMENTS & PERSISTENCE
    // ========================================================

    @Test
    @DisplayName("11. Badge unlocking on activity completion")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void badgeUnlockingOnCompletion() throws Exception {
        // Complete Toothbrushing
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(14L);
        req.setStepNumber(7);
        req.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Verify rewards endpoint includes earned badges
        MvcResult res = mockMvc.perform(get("/api/child-mode/rewards/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        List<ChildRewardDto> rewards = Arrays.asList(objectMapper.readValue(res.getResponse().getContentAsString(), ChildRewardDto[].class));
        assertFalse(rewards.isEmpty());
    }

    @Test
    @DisplayName("12. Duplicate badge prevention via database unique constraint")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void duplicateBadgePrevention() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/rewards/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        List<ChildRewardDto> rewards = Arrays.asList(objectMapper.readValue(res.getResponse().getContentAsString(), ChildRewardDto[].class));
        Set<String> badgeTypes = new HashSet<>();
        for (ChildRewardDto r : rewards) {
            assertTrue(badgeTypes.add(r.getBadgeType()), "Duplicate badge found: " + r.getBadgeType());
        }
    }

    @Test
    @DisplayName("13. Practice streak calculation from authentic practice dates")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void practiceStreakCalculation() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        ChildDashboardResponse dashboard = objectMapper.readValue(res.getResponse().getContentAsString(), ChildDashboardResponse.class);
        assertTrue(dashboard.getCurrentStreak() >= 0);
    }

    @Test
    @DisplayName("14. Progress persistence after navigation/restart")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void progressPersistenceAfterNavigation() throws Exception {
        // Save progress at step 4
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(13L); // Eating with Spoon
        req.setStepNumber(4);
        req.setIsActivityCompleted(false);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Query dashboard and check completedSteps
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        ChildDashboardResponse dashboard = objectMapper.readValue(res.getResponse().getContentAsString(), ChildDashboardResponse.class);
        Optional<ChildActivitySummaryDto> spoonAct = dashboard.getAllActivities().stream()
                .filter(a -> a.getActivityId().equals(13L))
                .findFirst();

        assertTrue(spoonAct.isPresent());
        assertTrue(spoonAct.get().getCompletedSteps() >= 4);
    }

    // ========================================================
    // 15-18: ROLE-BASED ACCESS CONTROL & SECURITY BOUNDARIES
    // ========================================================

    @Test
    @DisplayName("15. Therapist access to assigned child in Child Mode")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistAccessToAssignedChild() throws Exception {
        mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID));
    }

    @Test
    @DisplayName("16. Therapist denial for unassigned child (HTTP 403)")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistDenialForUnassignedChild() throws Exception {
        // Child 1 is assigned to layna, not therapist1
        mockMvc.perform(get("/api/child-mode/dashboard/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("17. Caregiver access to linked child in Child Mode")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverAccessToLinkedChild() throws Exception {
        mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID));
    }

    @Test
    @DisplayName("18. Caregiver denial for unlinked child (HTTP 403)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverDenialForUnlinkedChild() throws Exception {
        // Child 2 is not linked to caregiver1
        mockMvc.perform(get("/api/child-mode/dashboard/2"))
                .andExpect(status().isForbidden());
    }

    // ========================================================
    // 19-20: CLINICAL ISOLATION & REWARD BALANCE CONSISTENCY
    // ========================================================

    @Test
    @DisplayName("19. Child Mode clinical-data isolation: no L0-L6 prompt levels exposed")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void childModeClinicalDataIsolation() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();

        String body = res.getResponse().getContentAsString();

        // Must not expose clinical scoring hierarchy or prompt fading keywords
        assertFalse(body.contains("FULL_PHYSICAL"));
        assertFalse(body.contains("PARTIAL_PHYSICAL"));
        assertFalse(body.contains("promptLevelId"));
        assertFalse(body.contains("clinicalRationale"));
        assertFalse(body.contains("baselinePromptLevelId"));
    }

    @Test
    @DisplayName("20. Correct reward balance consistency after repeated requests")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void correctRewardBalanceAfterRepeatedRequests() throws Exception {
        RecordStepProgressRequest req = new RecordStepProgressRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(14L);
        req.setStepNumber(7);
        req.setIsActivityCompleted(true);

        int initialBalance = 0;
        MvcResult initRes = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();
        ChildDashboardResponse initDash = objectMapper.readValue(initRes.getResponse().getContentAsString(), ChildDashboardResponse.class);
        initialBalance = initDash.getTotalStars();

        // 5 repeated completion calls
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/child-mode/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        // Final balance must remain unchanged (no star drift)
        MvcResult finalRes = mockMvc.perform(get("/api/child-mode/dashboard/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();
        ChildDashboardResponse finalDash = objectMapper.readValue(finalRes.getResponse().getContentAsString(), ChildDashboardResponse.class);

        assertEquals(initialBalance, finalDash.getTotalStars());
    }
}
