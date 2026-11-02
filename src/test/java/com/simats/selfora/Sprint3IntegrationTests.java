package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.AdaptivePlanDtos;
import com.simats.selfora.dto.AssessmentDtos;
import com.simats.selfora.dto.CaregiverPracticeDtos;
import com.simats.selfora.dto.HomeProgramDtos;
import com.simats.selfora.dto.SessionDtos;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Sprint3IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HomeProgramRepository homeProgramRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private PromptLevelRepository promptLevelRepository;

    // Child 13 is assigned to therapist1 AND linked to caregiver1
    private static final Long TEST_CHILD_ID = 13L;

    // ========================================================
    // 1-5: CLINICAL ASSESSMENT & PROMPT HIERARCHY
    // ========================================================

    @Test
    @DisplayName("1. Clinical assessment creation by assigned therapist")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanCreateAssessment() throws Exception {
        AssessmentDtos.StepAssessmentRequest step1 = new AssessmentDtos.StepAssessmentRequest(1L, 0, "COMPLETED", "Child completed independently");
        AssessmentDtos.StepAssessmentRequest step2 = new AssessmentDtos.StepAssessmentRequest(2L, 3, "COMPLETED", "Needed verbal cue");

        AssessmentDtos.CreateAssessmentRequest request = new AssessmentDtos.CreateAssessmentRequest(
                TEST_CHILD_ID, 1L, "Baseline assessment for Child", Arrays.asList(step1, step2)
        );

        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID))
                .andExpect(jsonPath("$.activityId").value(1))
                .andExpect(jsonPath("$.stepResults").isArray());
    }

    @Test
    @DisplayName("2. Clinical assessment retrieval by ID")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanRetrieveAssessmentById() throws Exception {
        AssessmentDtos.StepAssessmentRequest step = new AssessmentDtos.StepAssessmentRequest(1L, 0, "COMPLETED", "Notes");
        AssessmentDtos.CreateAssessmentRequest request = new AssessmentDtos.CreateAssessmentRequest(
                TEST_CHILD_ID, 1L, "Notes", Collections.singletonList(step)
        );

        MvcResult result = mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AssessmentDtos.AssessmentResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AssessmentDtos.AssessmentResponse.class);

        mockMvc.perform(get("/api/assessments/" + response.getAssessmentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assessmentId").value(response.getAssessmentId()))
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID));
    }

    @Test
    @DisplayName("3. Clinical assessment child history retrieval")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanRetrieveAssessmentsForChild() throws Exception {
        mockMvc.perform(get("/api/assessments/child/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("4. L0-L6 prompt-level persistence")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void l0ThroughL6PromptLevelsPersistAccurately() throws Exception {
        AssessmentDtos.StepAssessmentRequest stepL0 = new AssessmentDtos.StepAssessmentRequest(1L, 0, "COMPLETED", "Independent");
        AssessmentDtos.StepAssessmentRequest stepL3 = new AssessmentDtos.StepAssessmentRequest(2L, 3, "COMPLETED", "Verbal Prompt");
        AssessmentDtos.StepAssessmentRequest stepL6 = new AssessmentDtos.StepAssessmentRequest(3L, 6, "COMPLETED", "Full Physical");

        AssessmentDtos.CreateAssessmentRequest request = new AssessmentDtos.CreateAssessmentRequest(
                TEST_CHILD_ID, 1L, "Hierarchy evaluation", Arrays.asList(stepL0, stepL3, stepL6)
        );

        MvcResult result = mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AssessmentDtos.AssessmentResponse res = objectMapper.readValue(result.getResponse().getContentAsString(), AssessmentDtos.AssessmentResponse.class);
        assertEquals(3, res.getStepResults().size());
        assertEquals(0, res.getStepResults().get(0).getPromptLevelId());
        assertEquals(3, res.getStepResults().get(1).getPromptLevelId());
        assertEquals(6, res.getStepResults().get(2).getPromptLevelId());
    }

    @Test
    @DisplayName("5. Unable and Not Assessed outcomes handling")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void unableAndNotAssessedHandledDistinctly() throws Exception {
        AssessmentDtos.StepAssessmentRequest stepUnable = new AssessmentDtos.StepAssessmentRequest(1L, null, "UNABLE", "Child refused step");
        AssessmentDtos.StepAssessmentRequest stepNotAssessed = new AssessmentDtos.StepAssessmentRequest(2L, null, "NOT_ASSESSED", "Physical constraint noted");

        AssessmentDtos.CreateAssessmentRequest request = new AssessmentDtos.CreateAssessmentRequest(
                TEST_CHILD_ID, 1L, "Clinical outcome test", Arrays.asList(stepUnable, stepNotAssessed)
        );

        MvcResult result = mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AssessmentDtos.AssessmentResponse res = objectMapper.readValue(result.getResponse().getContentAsString(), AssessmentDtos.AssessmentResponse.class);
        assertEquals("UNABLE", res.getStepResults().get(0).getOutcome());
        assertEquals("NOT_ASSESSED", res.getStepResults().get(1).getOutcome());
        assertEquals("Physical constraint noted", res.getStepResults().get(1).getNotes());
    }

    // ========================================================
    // 6-7: ADL GOAL ASSIGNMENT
    // ========================================================

    @Test
    @DisplayName("6. ADL goal assignment via HomeProgram creation")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanAssignHomeProgramGoal() throws Exception {
        HomeProgramDtos.CreateHomeProgramRequest req = new HomeProgramDtos.CreateHomeProgramRequest();
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(1L);
        req.setTitle("T-Shirt Morning Practice");
        req.setDescription("Practice putting on T-shirt");
        req.setFrequencyPerWeek(4);
        req.setTargetDurationMinutes(15);
        req.setTargetPromptLevelId(2);
        req.setGoalStatement("Target: Achieve Level 2 Gesture Independence");
        req.setCaregiverInstructions("Point to neck opening first");

        mockMvc.perform(post("/api/home-programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID))
                .andExpect(jsonPath("$.title").value("T-Shirt Morning Practice"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("7. AdaptivePlan creation and approval")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanCreateAdaptivePlan() throws Exception {
        SessionDtos.StartSessionRequest sessionReq = new SessionDtos.StartSessionRequest(TEST_CHILD_ID, 1L, "DIRECT_THERAPY", "CLINIC");
        MvcResult sessionRes = mockMvc.perform(post("/api/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionReq)))
                .andExpect(status().isOk())
                .andReturn();
        SessionDtos.SessionResponse session = objectMapper.readValue(sessionRes.getResponse().getContentAsString(), SessionDtos.SessionResponse.class);

        AdaptivePlanDtos.CreateAdaptivePlanRequest req = new AdaptivePlanDtos.CreateAdaptivePlanRequest(
                TEST_CHILD_ID, 1L, session.getSessionId(), 4, 3, Arrays.asList(1L, 2L), "Fading model prompt to verbal cue", true
        );

        mockMvc.perform(post("/api/adaptive-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(TEST_CHILD_ID))
                .andExpect(jsonPath("$.isConfirmed").value(true));
    }

    // ========================================================
    // 8-11: CAREGIVER PRACTICE SUBMISSION & IDEMPOTENCY
    // ========================================================

    @Test
    @DisplayName("8. Caregiver practice submission supporting all 5 outcomes")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverCanSubmitPracticeWithAllFiveOutcomes() throws Exception {
        HomeProgramDtos.HomeProgramResponse hp = getOrCreateHomeProgramForChild(TEST_CHILD_ID);

        String clientSessionId = UUID.randomUUID().toString();
        CaregiverPracticeDtos.SubmitPracticeRequest req = new CaregiverPracticeDtos.SubmitPracticeRequest();
        req.setClientSessionId(clientSessionId);
        req.setHomeProgramId(hp.getId());
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(hp.getActivityId());
        req.setPracticeDate(LocalDate.now());
        req.setDurationMinutes(18);
        req.setCaregiverNotes("Great morning practice session");

        CaregiverPracticeDtos.StepResultItemDto s1 = new CaregiverPracticeDtos.StepResultItemDto(1L, 1, "Look at T-shirt", "INDEPENDENT", "Did it alone");
        CaregiverPracticeDtos.StepResultItemDto s2 = new CaregiverPracticeDtos.StepResultItemDto(2L, 2, "Pick up T-shirt", "LITTLE_HELP", "Gentle nudge");
        CaregiverPracticeDtos.StepResultItemDto s3 = new CaregiverPracticeDtos.StepResultItemDto(3L, 3, "Identify front", "LOT_HELP", "Hand guidance");
        CaregiverPracticeDtos.StepResultItemDto s4 = new CaregiverPracticeDtos.StepResultItemDto(4L, 4, "Find neck opening", "COULD_NOT_COMPLETE", "Got frustrated");
        CaregiverPracticeDtos.StepResultItemDto s5 = new CaregiverPracticeDtos.StepResultItemDto(5L, 5, "Hold shoulders", "NOT_PRACTISED", "Skipped for time");
        req.setStepResults(Arrays.asList(s1, s2, s3, s4, s5));

        mockMvc.perform(post("/api/caregiver-practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSessionId").value(clientSessionId))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.stepResults").isArray());
    }

    @Test
    @DisplayName("9. Duplicate submission idempotency via clientSessionId")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void duplicateSubmissionReturnsSameSessionWithoutDuplication() throws Exception {
        HomeProgramDtos.HomeProgramResponse hp = getOrCreateHomeProgramForChild(TEST_CHILD_ID);
        String clientSessionId = UUID.randomUUID().toString();

        CaregiverPracticeDtos.SubmitPracticeRequest req = new CaregiverPracticeDtos.SubmitPracticeRequest();
        req.setClientSessionId(clientSessionId);
        req.setHomeProgramId(hp.getId());
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(hp.getActivityId());
        req.setPracticeDate(LocalDate.now());
        req.setDurationMinutes(15);
        req.setCaregiverNotes("Initial submission");
        req.setStepResults(Collections.singletonList(
                new CaregiverPracticeDtos.StepResultItemDto(1L, 1, "Look", "INDEPENDENT", "Good")
        ));

        // First attempt
        MvcResult res1 = mockMvc.perform(post("/api/caregiver-practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        CaregiverPracticeDtos.CaregiverPracticeSessionResponse session1 = objectMapper.readValue(res1.getResponse().getContentAsString(), CaregiverPracticeDtos.CaregiverPracticeSessionResponse.class);

        // Second attempt with exact same clientSessionId
        MvcResult res2 = mockMvc.perform(post("/api/caregiver-practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        CaregiverPracticeDtos.CaregiverPracticeSessionResponse session2 = objectMapper.readValue(res2.getResponse().getContentAsString(), CaregiverPracticeDtos.CaregiverPracticeSessionResponse.class);

        // IDs must be identical (no duplicate record created)
        assertEquals(session1.getId(), session2.getId());
        assertEquals(clientSessionId, session2.getClientSessionId());
    }

    @Test
    @DisplayName("10. New sessions on same day with distinct clientSessionId succeed")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void multipleSessionsOnSameDaySucceedWithDistinctClientSessionId() throws Exception {
        HomeProgramDtos.HomeProgramResponse hp = getOrCreateHomeProgramForChild(TEST_CHILD_ID);

        String id1 = UUID.randomUUID().toString();
        CaregiverPracticeDtos.SubmitPracticeRequest req1 = new CaregiverPracticeDtos.SubmitPracticeRequest();
        req1.setClientSessionId(id1);
        req1.setHomeProgramId(hp.getId());
        req1.setChildId(TEST_CHILD_ID);
        req1.setActivityId(hp.getActivityId());
        req1.setPracticeDate(LocalDate.now());
        req1.setDurationMinutes(10);
        req1.setCaregiverNotes("Morning session");

        MvcResult res1 = mockMvc.perform(post("/api/caregiver-practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andReturn();

        String id2 = UUID.randomUUID().toString();
        CaregiverPracticeDtos.SubmitPracticeRequest req2 = new CaregiverPracticeDtos.SubmitPracticeRequest();
        req2.setClientSessionId(id2);
        req2.setHomeProgramId(hp.getId());
        req2.setChildId(TEST_CHILD_ID);
        req2.setActivityId(hp.getActivityId());
        req2.setPracticeDate(LocalDate.now());
        req2.setDurationMinutes(12);
        req2.setCaregiverNotes("Evening session");

        MvcResult res2 = mockMvc.perform(post("/api/caregiver-practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andReturn();

        CaregiverPracticeDtos.CaregiverPracticeSessionResponse s1 = objectMapper.readValue(res1.getResponse().getContentAsString(), CaregiverPracticeDtos.CaregiverPracticeSessionResponse.class);
        CaregiverPracticeDtos.CaregiverPracticeSessionResponse s2 = objectMapper.readValue(res2.getResponse().getContentAsString(), CaregiverPracticeDtos.CaregiverPracticeSessionResponse.class);

        assertNotEquals(s1.getId(), s2.getId());
    }

    @Test
    @DisplayName("11. Practice history retrieval for linked child")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverCanRetrievePracticeHistoryForLinkedChild() throws Exception {
        mockMvc.perform(get("/api/caregiver-practice/history/child/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ========================================================
    // 12-16: THERAPIST REVIEW & SECURITY ACCESS
    // ========================================================

    @Test
    @DisplayName("12. Therapist review submission updates status without data loss")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanReviewCaregiverSubmission() throws Exception {
        HomeProgramDtos.HomeProgramResponse hp = getOrCreateHomeProgramForChild(TEST_CHILD_ID);
        CaregiverPracticeDtos.SubmitPracticeRequest req = new CaregiverPracticeDtos.SubmitPracticeRequest();
        req.setClientSessionId(UUID.randomUUID().toString());
        req.setHomeProgramId(hp.getId());
        req.setChildId(TEST_CHILD_ID);
        req.setActivityId(hp.getActivityId());
        req.setPracticeDate(LocalDate.now());
        req.setDurationMinutes(15);
        req.setCaregiverNotes("Caregiver notes to be reviewed");

        MvcResult submitResult = mockMvc.perform(post("/api/caregiver-practice/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        CaregiverPracticeDtos.CaregiverPracticeSessionResponse submitted = objectMapper.readValue(submitResult.getResponse().getContentAsString(), CaregiverPracticeDtos.CaregiverPracticeSessionResponse.class);

        // Therapist submits review
        CaregiverPracticeDtos.TherapistReviewRequest reviewReq = new CaregiverPracticeDtos.TherapistReviewRequest();
        reviewReq.setStatus("REVIEWED");
        reviewReq.setFeedbackText("Excellent technique observed. Continue visual prompting on step 3.");
        reviewReq.setFlaggedForFollowup(false);

        mockMvc.perform(post("/api/caregiver-practice/reviews/" + submitted.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"))
                .andExpect(jsonPath("$.therapistFeedback").value("Excellent technique observed. Continue visual prompting on step 3."))
                .andExpect(jsonPath("$.caregiverNotes").value("Caregiver notes to be reviewed"));
    }

    @Test
    @DisplayName("13. Cross-therapist data isolation: therapist1 cannot access Child 1 (assigned to therapist layn)")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void unassignedTherapistCannotAccessOtherTherapistChildAssessment() throws Exception {
        // Child 1 is assigned to layna (ID 5), not therapist1
        mockMvc.perform(get("/api/assessments/child/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("14. Cross-caregiver data isolation: caregiver1 cannot access Child 2 (unlinked)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void unlinkedCaregiverCannotAccessChildPracticeHistory() throws Exception {
        // Child 2 is not linked to caregiver1
        mockMvc.perform(get("/api/caregiver-practice/history/child/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. Caregiver forbidden from submitting therapist review")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverForbiddenFromSubmittingTherapistReview() throws Exception {
        CaregiverPracticeDtos.TherapistReviewRequest reviewReq = new CaregiverPracticeDtos.TherapistReviewRequest();
        reviewReq.setStatus("REVIEWED");
        reviewReq.setFeedbackText("Caregiver attempting to review");

        mockMvc.perform(post("/api/caregiver-practice/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("16. Caregiver forbidden from creating clinical assessment")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void caregiverForbiddenFromCreatingAssessment() throws Exception {
        AssessmentDtos.CreateAssessmentRequest request = new AssessmentDtos.CreateAssessmentRequest(
                TEST_CHILD_ID, 1L, "Caregiver assessment attempt", Collections.emptyList()
        );

        mockMvc.perform(post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========================================================
    // 17-20: REGRESSION & MULTI-ROLE VERIFICATION
    // ========================================================

    @Test
    @DisplayName("17. Super Admin can access child assessment history")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void superAdminCanAccessAssessmentsForAnyChild() throws Exception {
        mockMvc.perform(get("/api/assessments/child/" + TEST_CHILD_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("18. Super Admin can access all pending practice reviews")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void superAdminCanAccessAllPendingReviews() throws Exception {
        mockMvc.perform(get("/api/caregiver-practice/pending-reviews"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("19. Therapy session retrieval by child ID")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void therapistCanRetrieveTherapySessionsForChild() throws Exception {
        mockMvc.perform(get("/api/sessions/child/" + TEST_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("20. Task steps retrieval for activity")
    @WithMockUser(username = "therapist1", roles = {"THERAPIST"})
    void canRetrieveTaskStepsForActivity() throws Exception {
        mockMvc.perform(get("/api/activities/1/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Helper to get or create Home Program for test child
    private HomeProgramDtos.HomeProgramResponse getOrCreateHomeProgramForChild(Long childId) {
        var existing = homeProgramRepository.findTopByChildIdAndIsActiveTrueOrderByCreatedAtDesc(childId);
        if (existing.isPresent()) {
            HomeProgram p = existing.get();
            return HomeProgramDtos.HomeProgramResponse.builder()
                    .id(p.getId())
                    .childId(p.getChild().getId())
                    .activityId(p.getActivity().getId())
                    .title(p.getTitle())
                    .status(p.getStatus())
                    .build();
        }

        Child child = childRepository.findById(childId).orElseThrow();
        User therapist = userRepository.findByUsername("therapist1").orElseThrow();
        Activity activity = activityRepository.findById(1L).orElseThrow();
        PromptLevel promptLevel = promptLevelRepository.findById(2).orElseThrow();

        HomeProgram hp = HomeProgram.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .title("T-Shirt Morning Practice")
                .description("Practice putting on T-shirt")
                .frequencyPerWeek(4)
                .targetDurationMinutes(15)
                .targetPromptLevel(promptLevel)
                .goalStatement("Target: Achieve Level 2 Gesture Independence")
                .caregiverInstructions("Point to neck opening first")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .isActive(true)
                .build();

        HomeProgram saved = homeProgramRepository.save(hp);
        return HomeProgramDtos.HomeProgramResponse.builder()
                .id(saved.getId())
                .childId(saved.getChild().getId())
                .activityId(saved.getActivity().getId())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .build();
    }
}
