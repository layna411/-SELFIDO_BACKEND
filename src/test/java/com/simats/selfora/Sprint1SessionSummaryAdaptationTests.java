package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.AdaptivePlanDtos;
import com.simats.selfora.dto.SessionDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class Sprint1SessionSummaryAdaptationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    @Autowired
    private PromptLevelRepository promptLevelRepository;

    @Autowired
    private TherapySessionRepository sessionRepository;

    @Autowired
    private PerformanceRecordRepository performanceRecordRepository;

    @Autowired
    private AdaptivePlanRepository adaptivePlanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User therapistA;
    private User therapistB;
    private User caregiverUser;
    private Child childA;
    private Child childB;
    private Activity testActivity;
    private List<TaskStep> activitySteps;
    private PromptLevel promptL0;
    private PromptLevel promptL1;
    private PromptLevel promptL2;
    private PromptLevel promptL3;
    private PromptLevel promptUnable;
    private PromptLevel promptNotAssessed;

    @BeforeEach
    void setUp() {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_THERAPIST").description("Therapist").build()));
        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CAREGIVER").description("Caregiver").build()));

        String runId = UUID.randomUUID().toString().substring(0, 8);

        therapistA = new User();
        therapistA.setUsername("sprint1_therapist_a_" + runId);
        therapistA.setPasswordHash(passwordEncoder.encode("Password@123"));
        therapistA.setFullName("Dr. Sarah Jenkins");
        therapistA.setEmail("sarah_" + runId + "@selfora.com");
        therapistA.setIsActive(true);
        therapistA.setRoles(new HashSet<>(Collections.singletonList(therapistRole)));
        therapistA = userRepository.save(therapistA);

        therapistB = new User();
        therapistB.setUsername("sprint1_therapist_b_" + runId);
        therapistB.setPasswordHash(passwordEncoder.encode("Password@123"));
        therapistB.setFullName("Dr. Bob Clinician");
        therapistB.setEmail("bob_" + runId + "@selfora.com");
        therapistB.setIsActive(true);
        therapistB.setRoles(new HashSet<>(Collections.singletonList(therapistRole)));
        therapistB = userRepository.save(therapistB);

        caregiverUser = new User();
        caregiverUser.setUsername("sprint1_caregiver_" + runId);
        caregiverUser.setPasswordHash(passwordEncoder.encode("Password@123"));
        caregiverUser.setFullName("Priya Sharma");
        caregiverUser.setEmail("priya_" + runId + "@selfora.com");
        caregiverUser.setIsActive(true);
        caregiverUser.setRoles(new HashSet<>(Collections.singletonList(caregiverRole)));
        caregiverUser = userRepository.save(caregiverUser);

        childA = Child.builder()
                .firstName("Aarav")
                .lastName("Sharma")
                .dateOfBirth(LocalDate.of(2018, 5, 12))
                .gender("BOY")
                .diagnosisNotes("Sensory sensitivity, ADL fine motor development")
                .isActive(true)
                .build();
        childA = childRepository.save(childA);

        childB = Child.builder()
                .firstName("Ananya")
                .lastName("Roy")
                .dateOfBirth(LocalDate.of(2019, 8, 20))
                .gender("GIRL")
                .diagnosisNotes("Level 1 ASD, communication support")
                .isActive(true)
                .build();
        childB = childRepository.save(childB);

        // Assign childA to therapistA; assign childB to therapistA and therapistB
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistA.getId(), childA.getId());
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistA.getId(), childB.getId());
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistB.getId(), childB.getId());
        // Link caregiverUser to childA
        jdbcTemplate.update("INSERT INTO caregiver_children (caregiver_id, child_id) VALUES (?, ?)", caregiverUser.getId(), childA.getId());

        // Use activity 14 (Toothbrushing - exactly 7 steps) or fallback to activity 1
        testActivity = activityRepository.findById(14L).orElseGet(() -> activityRepository.findById(1L).orElseThrow());
        activitySteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(testActivity.getId());

        promptL0 = promptLevelRepository.findById(0).orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(0).levelCode("INDEPENDENT").levelName("Independent").hierarchyOrder(0).build()));
        promptL1 = promptLevelRepository.findById(1).orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(1).levelCode("VISUAL").levelName("Visual Prompt").hierarchyOrder(1).build()));
        promptL2 = promptLevelRepository.findById(2).orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(2).levelCode("GESTURE").levelName("Gesture Prompt").hierarchyOrder(2).build()));
        promptL3 = promptLevelRepository.findById(3).orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(3).levelCode("VERBAL").levelName("Verbal Prompt").hierarchyOrder(3).build()));
        promptUnable = promptLevelRepository.findById(7).orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(7).levelCode("UNABLE").levelName("Unable").hierarchyOrder(7).build()));
        promptNotAssessed = promptLevelRepository.findById(8).orElseGet(() -> promptLevelRepository.save(PromptLevel.builder().id(8).levelCode("NOT_ASSESSED").levelName("Not Assessed").hierarchyOrder(8).build()));
    }

    @Test
    @DisplayName("1. Session summary with real saved session data: verify dynamic child, code, duration, steps and metrics")
    void testRealSessionSummaryData() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusMinutes(25);
        LocalDateTime end = LocalDateTime.now();

        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-REAL-TEST-001")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(start)
                .endTime(end)
                .totalDurationSeconds(1500)
                .summaryNotes("Aarav showed excellent engagement with visual prompts.")
                .build();
        session = sessionRepository.save(session);

        // Record performance for step 1 (L0 Independent) and step 2 (L3 Verbal)
        if (activitySteps.size() >= 2) {
            PerformanceRecord r1 = PerformanceRecord.builder()
                    .session(session)
                    .step(activitySteps.get(0))
                    .promptLevel(promptL0)
                    .outcome("COMPLETED")
                    .attempts(1)
                    .durationSeconds(30)
                    .therapistNote("Completed without prompts")
                    .build();
            performanceRecordRepository.save(r1);

            PerformanceRecord r2 = PerformanceRecord.builder()
                    .session(session)
                    .step(activitySteps.get(1))
                    .promptLevel(promptL3)
                    .outcome("COMPLETED")
                    .attempts(2)
                    .durationSeconds(45)
                    .therapistNote("Needed one verbal reminder")
                    .build();
            performanceRecordRepository.save(r2);
        }

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is(session.getId().intValue())))
                .andExpect(jsonPath("$.sessionCode", is("SESS-REAL-TEST-001")))
                .andExpect(jsonPath("$.childId", is(childA.getId().intValue())))
                .andExpect(jsonPath("$.childName", is("Aarav Sharma")))
                .andExpect(jsonPath("$.activityTitle", is(testActivity.getTitle())))
                .andExpect(jsonPath("$.totalDurationSeconds", is(1500)))
                .andExpect(jsonPath("$.completedSteps", is(2)))
                .andExpect(jsonPath("$.independentSteps", is(1)))
                .andExpect(jsonPath("$.independencePercentage", is(50.0)))
                .andExpect(jsonPath("$.summaryNotes", containsString("excellent engagement")))
                .andExpect(jsonPath("$.performanceRecords", hasSize(2)))
                .andExpect(jsonPath("$.performanceRecords[0].therapistNote", is("Completed without prompts")))
                .andExpect(jsonPath("$.performanceRecords[1].therapistNote", is("Needed one verbal reminder")));
    }

    @Test
    @DisplayName("2. Session summary with different historical dates: verify exact date preservation and no current time override")
    void testSessionSummaryDifferentDates() throws Exception {
        LocalDateTime pastStart = LocalDateTime.of(2025, 4, 15, 14, 30, 0);
        LocalDateTime pastEnd = LocalDateTime.of(2025, 4, 15, 15, 15, 0);

        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-HISTORICAL-DATE")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(pastStart)
                .endTime(pastEnd)
                .totalDurationSeconds(2700)
                .build();
        session = sessionRepository.save(session);

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime", startsWith("2025-04-15T14:30:00")))
                .andExpect(jsonPath("$.endTime", startsWith("2025-04-15T15:15:00")))
                .andExpect(jsonPath("$.totalDurationSeconds", is(2700)));
    }

    @Test
    @DisplayName("3. Session summary with different duration: verify exact duration returned")
    void testSessionSummaryDifferentDurations() throws Exception {
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-DUR-840")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusSeconds(840))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(840) // 14 minutes
                .build();
        session = sessionRepository.save(session);

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDurationSeconds", is(840)));
    }

    @Test
    @DisplayName("4. Session with all steps completed: verify outcome is COMPLETED and completedSteps matches total")
    void testSessionWithAllStepsCompleted() throws Exception {
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-ALL-STEPS")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(20))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(1200)
                .build();
        session = sessionRepository.save(session);

        for (TaskStep step : activitySteps) {
            performanceRecordRepository.save(PerformanceRecord.builder()
                    .session(session)
                    .step(step)
                    .promptLevel(promptL0)
                    .outcome("COMPLETED")
                    .attempts(1)
                    .durationSeconds(15)
                    .build());
        }

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSteps", is(activitySteps.size())))
                .andExpect(jsonPath("$.completedSteps", is(activitySteps.size())))
                .andExpect(jsonPath("$.independentSteps", is(activitySteps.size())))
                .andExpect(jsonPath("$.independencePercentage", is(100.0)))
                .andExpect(jsonPath("$.sessionOutcome", is("COMPLETED")));
    }

    @Test
    @DisplayName("5. Session with partial completion: verify partial outcome and unable steps count")
    void testSessionWithPartialCompletion() throws Exception {
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-PARTIAL")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(600)
                .build();
        session = sessionRepository.save(session);

        if (activitySteps.size() >= 3) {
            performanceRecordRepository.save(PerformanceRecord.builder()
                    .session(session)
                    .step(activitySteps.get(0))
                    .promptLevel(promptL0)
                    .outcome("COMPLETED")
                    .attempts(1)
                    .durationSeconds(20)
                    .build());

            performanceRecordRepository.save(PerformanceRecord.builder()
                    .session(session)
                    .step(activitySteps.get(1))
                    .promptLevel(promptL3)
                    .outcome("COMPLETED")
                    .attempts(2)
                    .durationSeconds(40)
                    .build());

            performanceRecordRepository.save(PerformanceRecord.builder()
                    .session(session)
                    .step(activitySteps.get(2))
                    .promptLevel(promptUnable)
                    .outcome("UNABLE")
                    .attempts(3)
                    .durationSeconds(60)
                    .build());
        }

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", is(2)))
                .andExpect(jsonPath("$.unableSteps", is(1)))
                .andExpect(jsonPath("$.sessionOutcome", is("PARTIAL")));
    }

    @Test
    @DisplayName("6. Session with no recorded performance: returns 0 metrics and IN_PROGRESS outcome")
    void testSessionWithNoRecordedPerformance() throws Exception {
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-EMPTY")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now())
                .endTime(null) // in progress
                .totalDurationSeconds(0)
                .build();
        session = sessionRepository.save(session);

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSteps", is(0)))
                .andExpect(jsonPath("$.independentSteps", is(0)))
                .andExpect(jsonPath("$.independencePercentage", is(0.0)))
                .andExpect(jsonPath("$.sessionOutcome", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.performanceRecords", empty()));
    }

    @Test
    @DisplayName("7. Clinical Scoring Rule: UNABLE included in denominator (0%), NOT_ASSESSED excluded from denominator")
    void testCorrectIndependenceCalculation() throws Exception {
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-SCORING-RULES")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(15))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(900)
                .build();
        session = sessionRepository.save(session);

        // 2 Independent steps (L0)
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(0)).promptLevel(promptL0).outcome("COMPLETED").build());
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(1)).promptLevel(promptL0).outcome("COMPLETED").build());

        // 1 Assisted step (L3 Verbal)
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(2)).promptLevel(promptL3).outcome("COMPLETED").build());

        // 1 UNABLE step (PromptLevel 7 - counts in denominator as 0%)
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(3)).promptLevel(promptUnable).outcome("UNABLE").build());

        // 1 NOT_ASSESSED step (PromptLevel 8 - excluded from denominator)
        if (activitySteps.size() >= 5) {
            performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(4)).promptLevel(promptNotAssessed).outcome("NOT_ASSESSED").build());
        }

        // Calculation: Total assessed = 2 (L0) + 1 (L3) + 1 (UNABLE) = 4. (NOT_ASSESSED excluded).
        // Independent = 2.
        // Expected Independence = (2 / 4) * 100.0 = 50.0%.

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.independentSteps", is(2)))
                .andExpect(jsonPath("$.promptedSteps", is(1)))
                .andExpect(jsonPath("$.unableSteps", is(1)))
                .andExpect(jsonPath("$.notAssessedSteps", is(1)))
                .andExpect(jsonPath("$.independencePercentage", is(50.0)));
    }

    @Test
    @DisplayName("8. Prompt level distribution and most frequent prompt level")
    void testPromptLevelHistoryAndDistribution() throws Exception {
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-DISTRIB")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(15))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(900)
                .build();
        session = sessionRepository.save(session);

        // 1 L1, 3 L3, 1 L0
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(0)).promptLevel(promptL1).outcome("COMPLETED").build());
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(1)).promptLevel(promptL3).outcome("COMPLETED").build());
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(2)).promptLevel(promptL3).outcome("COMPLETED").build());
        performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(3)).promptLevel(promptL3).outcome("COMPLETED").build());
        if (activitySteps.size() >= 5) {
            performanceRecordRepository.save(PerformanceRecord.builder().session(session).step(activitySteps.get(4)).promptLevel(promptL0).outcome("COMPLETED").build());
        }

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptDistribution.VERBAL", is(3)))
                .andExpect(jsonPath("$.promptDistribution.VISUAL", is(1)))
                .andExpect(jsonPath("$.mostFrequentPromptLevel", is("VERBAL")));
    }

    @Test
    @DisplayName("9. Adaptation for two different children: verify strict scoping and zero cross-child leakage")
    void testAdaptationForTwoDifferentChildren() throws Exception {
        // Create session for Child A
        TherapySession sessionA = sessionRepository.save(TherapySession.builder()
                .sessionCode("SESS-ADAPT-A")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(20))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(1200)
                .build());

        // Create session for Child B
        TherapySession sessionB = sessionRepository.save(TherapySession.builder()
                .sessionCode("SESS-ADAPT-B")
                .child(childB)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(15))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(900)
                .build());

        // Create Adaptive Plan for Child A
        AdaptivePlanDtos.CreateAdaptivePlanRequest reqA = new AdaptivePlanDtos.CreateAdaptivePlanRequest(
                childA.getId(),
                testActivity.getId(),
                sessionA.getId(),
                3, // Verbal
                1, // Visual
                Arrays.asList(activitySteps.get(0).getId()),
                "Aarav needs visual cues for Step 1",
                true
        );

        mockMvc.perform(post("/api/adaptive-plans")
                        .with(user(therapistA.getUsername()).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId", is(childA.getId().intValue())))
                .andExpect(jsonPath("$.childName", is("Aarav Sharma")))
                .andExpect(jsonPath("$.targetPromptLevelId", is(1)))
                .andExpect(jsonPath("$.isConfirmed", is(true)));

        // Create Adaptive Plan for Child B
        AdaptivePlanDtos.CreateAdaptivePlanRequest reqB = new AdaptivePlanDtos.CreateAdaptivePlanRequest(
                childB.getId(),
                testActivity.getId(),
                sessionB.getId(),
                2, // Gesture
                0, // Independent
                Arrays.asList(activitySteps.get(1).getId()),
                "Ananya ready for independent transition on Step 2",
                true
        );

        mockMvc.perform(post("/api/adaptive-plans")
                        .with(user(therapistA.getUsername()).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId", is(childB.getId().intValue())))
                .andExpect(jsonPath("$.childName", is("Ananya Roy")))
                .andExpect(jsonPath("$.targetPromptLevelId", is(0)));

        // Verify Child A plans only return Child A's plan
        mockMvc.perform(get("/api/adaptive-plans/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].childId", is(childA.getId().intValue())))
                .andExpect(jsonPath("$[0].clinicalRationale", containsString("Aarav needs visual cues")));

        // Verify Child B plans only return Child B's plan
        mockMvc.perform(get("/api/adaptive-plans/child/" + childB.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].childId", is(childB.getId().intValue())))
                .andExpect(jsonPath("$[0].clinicalRationale", containsString("Ananya ready for independent")));
    }

    @Test
    @DisplayName("10. API contract validation: SessionResponse contains all fields required for navigation")
    void testNavigationArgumentsAndContract() throws Exception {
        TherapySession session = sessionRepository.save(TherapySession.builder()
                .sessionCode("SESS-CONTRACT-CHECK")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(1800)
                .build());

        mockMvc.perform(get("/api/sessions/" + session.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.childId", is(childA.getId().intValue())))
                .andExpect(jsonPath("$.activityId", is(testActivity.getId().intValue())))
                .andExpect(jsonPath("$.childName", notNullValue()))
                .andExpect(jsonPath("$.activityTitle", notNullValue()))
                .andExpect(jsonPath("$.totalSteps", notNullValue()));
    }

    @Test
    @DisplayName("11. Security: Unauthorized therapist cannot access another therapist's child session (HTTP 403)")
    void testUnauthorizedTherapistAccess() throws Exception {
        // sessionA belongs to childA, who is assigned only to therapistA (NOT therapistB)
        TherapySession sessionA = sessionRepository.save(TherapySession.builder()
                .sessionCode("SESS-ISOLATED-A")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(600)
                .build());

        mockMvc.perform(get("/api/sessions/" + sessionA.getId())
                        .with(user(therapistB.getUsername()).roles("THERAPIST")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("12. Security: Caregiver cannot access private clinical session summaries or therapist notes (HTTP 403)")
    void testCaregiverAccessBlockedToClinicalSession() throws Exception {
        TherapySession sessionA = sessionRepository.save(TherapySession.builder()
                .sessionCode("SESS-CLINICAL-PRIVATE")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(15))
                .endTime(LocalDateTime.now())
                .totalDurationSeconds(900)
                .summaryNotes("Private clinical diagnostic notes")
                .build());

        mockMvc.perform(get("/api/sessions/" + sessionA.getId())
                        .with(user(caregiverUser.getUsername()).roles("CAREGIVER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("13. Missing session ID: returns HTTP 404 ResourceNotFoundException")
    void testMissingSessionId() throws Exception {
        mockMvc.perform(get("/api/sessions/9999999")
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("14. Persistence: completeSession preserves original endTime and duration without rewriting historical data")
    void testPersistenceAfterSessionComplete() throws Exception {
        LocalDateTime originalStart = LocalDateTime.of(2025, 5, 1, 10, 0, 0);
        LocalDateTime originalEnd = LocalDateTime.of(2025, 5, 1, 10, 45, 0);

        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-IDEMPOTENT-COMPLETE")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(originalStart)
                .endTime(originalEnd)
                .totalDurationSeconds(2700)
                .build();
        session = sessionRepository.save(session);

        // Call completeSession on already completed session
        mockMvc.perform(post("/api/sessions/" + session.getId() + "/complete")
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is(session.getId().intValue())))
                .andExpect(jsonPath("$.totalDurationSeconds", is(2700)));

        // Verify database record still has original end time and duration (not overwritten by now())
        TherapySession reloaded = sessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(originalEnd, reloaded.getEndTime(), "Original historical endTime must be preserved");
        assertEquals(2700, reloaded.getTotalDurationSeconds(), "Original historical duration must be preserved");
    }
}
