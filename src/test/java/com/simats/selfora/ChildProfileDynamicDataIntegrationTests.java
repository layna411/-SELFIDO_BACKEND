package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.adaptation.PromptFadingService;
import com.simats.selfora.dto.PromptFadingDtos;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class ChildProfileDynamicDataIntegrationTests {

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
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

    @Autowired
    private HomeProgramRepository homeProgramRepository;

    @Autowired
    private CaregiverPracticeSessionRepository caregiverPracticeSessionRepository;

    @Autowired
    private PromptFadingService promptFadingService;

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
    private TaskStep testStep1;
    private TaskStep testStep2;
    private PromptLevel promptLevel0;
    private PromptLevel promptLevel3;

    @BeforeEach
    void setUp() {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_THERAPIST").description("Therapist").build()));
        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CAREGIVER").description("Caregiver").build()));

        String runId = UUID.randomUUID().toString().substring(0, 8);

        therapistA = new User();
        therapistA.setUsername("therapist_a_" + runId);
        therapistA.setPasswordHash(passwordEncoder.encode("Password@123"));
        therapistA.setFullName("Dr. Alice Jenkins");
        therapistA.setEmail("therapist_a_" + runId + "@selfora.com");
        therapistA.setIsActive(true);
        therapistA.setRoles(new HashSet<>(Collections.singletonList(therapistRole)));
        therapistA = userRepository.save(therapistA);

        therapistB = new User();
        therapistB.setUsername("therapist_b_" + runId);
        therapistB.setPasswordHash(passwordEncoder.encode("Password@123"));
        therapistB.setFullName("Dr. Bob Smith");
        therapistB.setEmail("therapist_b_" + runId + "@selfora.com");
        therapistB.setIsActive(true);
        therapistB.setRoles(new HashSet<>(Collections.singletonList(therapistRole)));
        therapistB = userRepository.save(therapistB);

        caregiverUser = new User();
        caregiverUser.setUsername("caregiver_" + runId);
        caregiverUser.setPasswordHash(passwordEncoder.encode("Password@123"));
        caregiverUser.setFullName("Maria Caregiver");
        caregiverUser.setEmail("maria_" + runId + "@selfora.com");
        caregiverUser.setPhone("+91 9123456789");
        caregiverUser.setIsActive(true);
        caregiverUser.setRoles(new HashSet<>(Collections.singletonList(caregiverRole)));
        caregiverUser = userRepository.save(caregiverUser);

        childA = Child.builder()
                .firstName("Aarav")
                .lastName("Verma")
                .dateOfBirth(LocalDate.of(2018, 5, 12))
                .gender("BOY")
                .diagnosisNotes("Level 2 ASD, sensory sensitivity")
                .isActive(true)
                .build();
        childA = childRepository.save(childA);

        childB = Child.builder()
                .firstName("Diya")
                .lastName("Sharma")
                .dateOfBirth(LocalDate.of(2019, 8, 20))
                .gender("GIRL")
                .diagnosisNotes("Level 1 ASD, communication support")
                .isActive(true)
                .build();
        childB = childRepository.save(childB);

        // Assign childA to therapistA
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistA.getId(), childA.getId());
        // Assign childB to therapistB
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistB.getId(), childB.getId());
        // Link caregiverUser to childA
        jdbcTemplate.update("INSERT INTO caregiver_children (caregiver_id, child_id) VALUES (?, ?)", caregiverUser.getId(), childA.getId());

        testActivity = activityRepository.findAll().stream().findFirst().orElseGet(() -> {
            Activity a = Activity.builder()
                    .title("Dressing Skills")
                    .description("Independent dressing sequence")
                    .targetGender("ALL")
                    .isActive(true)
                    .build();
            return activityRepository.save(a);
        });

        List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(testActivity.getId());
        if (steps.size() >= 2) {
            testStep1 = steps.get(0);
            testStep2 = steps.get(1);
        } else {
            testStep1 = taskStepRepository.save(TaskStep.builder()
                    .activity(testActivity).stepNumber(1).title("Step 1 Orient").instructionText("Orient").childInstruction("Look").build());
            testStep2 = taskStepRepository.save(TaskStep.builder()
                    .activity(testActivity).stepNumber(2).title("Step 2 Insert").instructionText("Insert").childInstruction("Put").build());
        }

        promptLevel0 = promptLevelRepository.findById(0).orElseThrow();
        promptLevel3 = promptLevelRepository.findById(3).orElseThrow();
    }

    private HomeProgram createTestHomeProgram(Child child, User therapist) {
        return homeProgramRepository.save(HomeProgram.builder()
                .child(child)
                .therapist(therapist)
                .activity(testActivity)
                .targetPromptLevel(promptLevel0)
                .title("Home Practice Dressing")
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().plusDays(21))
                .frequencyPerWeek(3)
                .targetDurationMinutes(15)
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("01. Therapist A accessing assigned Child A profile returns dynamic data including caregiver details")
    void test01_TherapistAccessAssignedChildProfile_ReturnsDynamicData() throws Exception {
        mockMvc.perform(get("/api/children/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(childA.getId()))
                .andExpect(jsonPath("$.firstName").value("Aarav"))
                .andExpect(jsonPath("$.lastName").value("Verma"))
                .andExpect(jsonPath("$.therapistName").value("Dr. Alice Jenkins"))
                .andExpect(jsonPath("$.caregiverName").value("Maria Caregiver"))
                .andExpect(jsonPath("$.caregiverEmail").value(caregiverUser.getEmail()))
                .andExpect(jsonPath("$.caregiverPhone").value("+91 9123456789"))
                .andExpect(jsonPath("$.caregiverId").value(caregiverUser.getId()))
                .andExpect(jsonPath("$.caregiverActive").value(true));
    }

    @Test
    @DisplayName("02. Therapist B accessing assigned Child B returns distinct isolated profile data")
    void test02_TherapistAccessDifferentAssignedChild_ReturnsDistinctProfile() throws Exception {
        mockMvc.perform(get("/api/children/" + childB.getId())
                        .with(user(therapistB.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(childB.getId()))
                .andExpect(jsonPath("$.firstName").value("Diya"))
                .andExpect(jsonPath("$.lastName").value("Sharma"))
                .andExpect(jsonPath("$.therapistName").value("Dr. Bob Smith"));
    }

    @Test
    @DisplayName("03. Loading real sessions for Child A returns actual persisted session records")
    void test03_GetSessionsForChild_ReturnsRealPersistedSessions() throws Exception {
        LocalDateTime sessionTime = LocalDateTime.of(2026, 11, 2, 10, 30);
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-" + UUID.randomUUID().toString().substring(0, 6))
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(sessionTime)
                .endTime(sessionTime.plusMinutes(25))
                .totalDurationSeconds(1500)
                .summaryNotes("Productive clinical session")
                .build();
        session = sessionRepository.save(session);

        PerformanceRecord rec = PerformanceRecord.builder()
                .session(session)
                .step(testStep1)
                .promptLevel(promptLevel0)
                .outcome("COMPLETED")
                .attempts(1)
                .durationSeconds(45)
                .therapistNote("Independent execution")
                .recordedAt(sessionTime.plusMinutes(5))
                .build();
        performanceRecordRepository.save(rec);

        mockMvc.perform(get("/api/sessions/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sessionId").value(session.getId()))
                .andExpect(jsonPath("$[0].totalDurationSeconds").value(1500))
                .andExpect(jsonPath("$[0].performanceRecords", hasSize(1)))
                .andExpect(jsonPath("$[0].performanceRecords[0].promptLevelId").value(0))
                .andExpect(jsonPath("$[0].performanceRecords[0].outcome").value("COMPLETED"));
    }

    @Test
    @DisplayName("04. Loading real assessment history for Child A returns actual persisted assessments")
    void test04_GetAssessmentsForChild_ReturnsRealPersistedAssessments() throws Exception {
        LocalDateTime assessDate = LocalDateTime.of(2026, 10, 15, 9, 30);
        Assessment assessment = Assessment.builder()
                .child(childA)
                .activity(testActivity)
                .therapist(therapistA)
                .assessmentDate(assessDate)
                .notes("Baseline evaluation of dressing skills")
                .build();
        assessment = assessmentRepository.save(assessment);

        AssessmentResult result = AssessmentResult.builder()
                .assessment(assessment)
                .step(testStep1)
                .baselinePromptLevel(promptLevel3)
                .outcome("COMPLETED")
                .notes("Needed verbal prompts")
                .build();
        assessmentResultRepository.save(result);

        mockMvc.perform(get("/api/assessments/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(assessment.getId()))
                .andExpect(jsonPath("$[0].therapistName").value("Dr. Alice Jenkins"))
                .andExpect(jsonPath("$[0].stepResults", hasSize(1)))
                .andExpect(jsonPath("$[0].stepResults[0].promptLevelId").value(3));
    }

    @Test
    @DisplayName("05. Switching between children loads isolated and distinct data")
    void test05_SwitchingChildren_LoadsIsolatedDistinctData() throws Exception {
        // Child A session
        TherapySession sessionA = TherapySession.builder()
                .sessionCode("SESS-A")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .startTime(LocalDateTime.now())
                .build();
        sessionRepository.save(sessionA);

        // Child B session
        TherapySession sessionB = TherapySession.builder()
                .sessionCode("SESS-B")
                .child(childB)
                .conductedByUser(therapistB)
                .activity(testActivity)
                .startTime(LocalDateTime.now())
                .build();
        sessionRepository.save(sessionB);

        // Child A query returns only sessionA
        mockMvc.perform(get("/api/sessions/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sessionCode").value("SESS-A"));

        // Child B query returns only sessionB
        mockMvc.perform(get("/api/sessions/child/" + childB.getId())
                        .with(user(therapistB.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sessionCode").value("SESS-B"));
    }

    @Test
    @DisplayName("06. Session timestamps match actual database records (no synthetic or hardcoded dates)")
    void test06_SessionTimestampsMatchActualDatabaseValues() throws Exception {
        LocalDateTime exactStartTime = LocalDateTime.of(2026, 9, 18, 14, 15, 30);
        TherapySession session = TherapySession.builder()
                .sessionCode("SESS-TIMESTAMP")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .startTime(exactStartTime)
                .build();
        sessionRepository.save(session);

        MvcResult result = mockMvc.perform(get("/api/sessions/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        assertTrue(responseStr.contains("2026-09-18T14:15:30"));
    }

    @Test
    @DisplayName("07. Empty session history returns empty list [] without mock fallback")
    void test07_EmptySessionHistory_ReturnsEmptyListNotMock() throws Exception {
        mockMvc.perform(get("/api/sessions/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("08. Empty assessment history returns empty list [] without mock fallback")
    void test08_EmptyAssessmentHistory_ReturnsEmptyListNotMock() throws Exception {
        mockMvc.perform(get("/api/assessments/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("09. Analyze & Adapt with real session records returns factual calculations")
    void test09_AnalyzeAndAdaptWithRealSessions_ReturnsFactualMetrics() throws Exception {
        LocalDateTime time = LocalDateTime.now();
        TherapySession session = sessionRepository.save(TherapySession.builder()
                .sessionCode("SESS-ADAPT")
                .child(childA)
                .conductedByUser(therapistA)
                .activity(testActivity)
                .startTime(time)
                .build());

        // 1 independent step, 1 verbal step
        performanceRecordRepository.save(PerformanceRecord.builder()
                .session(session).step(testStep1).promptLevel(promptLevel0)
                .outcome("COMPLETED").attempts(1).durationSeconds(30).recordedAt(time).build());
        performanceRecordRepository.save(PerformanceRecord.builder()
                .session(session).step(testStep2).promptLevel(promptLevel3)
                .outcome("COMPLETED").attempts(1).durationSeconds(30).recordedAt(time).build());

        mockMvc.perform(get("/api/children/" + childA.getId() + "/analyze-adapt?activityId=" + testActivity.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childName").value("Aarav Verma"))
                .andExpect(jsonPath("$.totalRecordedSessions").value(1))
                .andExpect(jsonPath("$.currentIndependencePercentage").value(50.0))
                .andExpect(jsonPath("$.activityCards", notNullValue()))
                .andExpect(jsonPath("$.factualObservations", notNullValue()));
    }

    @Test
    @DisplayName("10. Analyze & Adapt with zero sessions returns clean zeroed metrics without throwing 500")
    void test10_AnalyzeAndAdaptWithZeroSessions_ReturnsCleanZeroedMetricsWithoutCrashing() throws Exception {
        mockMvc.perform(get("/api/children/" + childA.getId() + "/analyze-adapt?activityId=" + testActivity.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childName").value("Aarav Verma"))
                .andExpect(jsonPath("$.totalRecordedSessions").value(0))
                .andExpect(jsonPath("$.currentIndependencePercentage").value(0.0))
                .andExpect(jsonPath("$.difficultSteps", hasSize(0)))
                .andExpect(jsonPath("$.independentSteps", hasSize(0)));
    }

    @Test
    @DisplayName("11. Prompt fading plans retrieval returns real persisted plans for child")
    void test11_PromptFadingPlans_RetrievalScopedToChild() throws Exception {
        PromptFadingDtos.CreatePromptFadingPlanRequest req = new PromptFadingDtos.CreatePromptFadingPlanRequest(
                childA.getId(),
                testActivity.getId(),
                testStep1.getId(),
                3,
                0,
                "Consistently completing step with visual cue",
                "3 consecutive sessions",
                "Fade to independent"
        );

        // Create plan as therapistA
        mockMvc.perform(post("/api/prompt-fading/plans")
                        .with(user(therapistA.getUsername()).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stepId").value(testStep1.getId()))
                .andExpect(jsonPath("$.currentPromptLevelId").value(3))
                .andExpect(jsonPath("$.targetPromptLevelId").value(0));

        // Retrieve plans for childA
        mockMvc.perform(get("/api/prompt-fading/plans/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].childName").value("Aarav Verma"))
                .andExpect(jsonPath("$[0].stepTitle").value(testStep1.getTitle()));
    }

    @Test
    @DisplayName("12. Prompt fading history returns auditable approved plan transitions")
    void test12_PromptFadingHistory_RetrievalScopedToChild() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(therapistA.getUsername(), null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_THERAPIST")))
        );

        PromptFadingDtos.CreatePromptFadingPlanRequest req = new PromptFadingDtos.CreatePromptFadingPlanRequest(
                childA.getId(),
                testActivity.getId(),
                testStep1.getId(),
                3,
                0,
                "Mastered with verbal prompt",
                "Fade to independent",
                "Approved in session"
        );

        PromptFadingDtos.PromptFadingPlanResponse plan = promptFadingService.createPromptFadingPlan(req);
        // Approve plan to log into history
        promptFadingService.approvePromptFadingPlan(plan.getId());

        mockMvc.perform(get("/api/prompt-fading/history/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stepNumber").value(testStep1.getStepNumber()))
                .andExpect(jsonPath("$[0].previousPromptLevelName").value("Verbal Prompt"))
                .andExpect(jsonPath("$[0].newPromptLevelName").value("Independent"));
    }

    @Test
    @DisplayName("13. Unauthorized therapist accessing child returns 403 Forbidden")
    void test13_UnauthorizedTherapistAccessingChild_Returns403Forbidden() throws Exception {
        // Therapist B is not assigned to Child A
        mockMvc.perform(get("/api/children/" + childA.getId())
                        .with(user(therapistB.getUsername()).roles("THERAPIST")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("14. Child reassignment dynamically transfers profile access to new therapist")
    void test14_ChildReassignmentDynamicallyTransfersAccess() throws Exception {
        // Therapist B cannot access Child A initially
        mockMvc.perform(get("/api/children/" + childA.getId())
                        .with(user(therapistB.getUsername()).roles("THERAPIST")))
                .andExpect(status().isForbidden());

        // Reassign Child A to Therapist B
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistB.getId(), childA.getId());

        // Therapist B now has authorized access
        mockMvc.perform(get("/api/children/" + childA.getId())
                        .with(user(therapistB.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(childA.getId()))
                .andExpect(jsonPath("$.firstName").value("Aarav"));
    }

    @Test
    @DisplayName("15. Caregiver practice sessions do not leak into therapist clinical session history")
    void test15_CaregiverPracticeDoesNotLeakIntoTherapistClinicalSessions() throws Exception {
        HomeProgram hp = createTestHomeProgram(childA, therapistA);

        CaregiverPracticeSession practiceSession = CaregiverPracticeSession.builder()
                .clientSessionId("CG-PRACTICE-" + UUID.randomUUID())
                .child(childA)
                .caregiver(caregiverUser)
                .homeProgram(hp)
                .activity(testActivity)
                .practiceDate(LocalDate.now())
                .status("APPROVED")
                .caregiverNotes("Completed independently at home")
                .durationMinutes(15)
                .build();
        caregiverPracticeSessionRepository.save(practiceSession);

        // Query therapist clinical sessions for Child A
        mockMvc.perform(get("/api/sessions/child/" + childA.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Stays 0 because caregiver practice is segregated
    }

    @Test
    @DisplayName("16. Caregiver practice summary in Analyze & Adapt reflects real home practice records")
    void test16_CaregiverSummaryInAnalyzeAdapt_ReflectsRealHomePractice() throws Exception {
        HomeProgram hp = createTestHomeProgram(childA, therapistA);

        CaregiverPracticeSession practiceSession = CaregiverPracticeSession.builder()
                .clientSessionId("CG-SUMMARY-" + UUID.randomUUID())
                .child(childA)
                .caregiver(caregiverUser)
                .homeProgram(hp)
                .activity(testActivity)
                .practiceDate(LocalDate.now())
                .status("APPROVED")
                .caregiverNotes("Practice went smoothly")
                .durationMinutes(20)
                .build();
        caregiverPracticeSessionRepository.save(practiceSession);

        mockMvc.perform(get("/api/children/" + childA.getId() + "/analyze-adapt?activityId=" + testActivity.getId())
                        .with(user(therapistA.getUsername()).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caregiverProgressSummary.totalSubmissions").value(1))
                .andExpect(jsonPath("$.caregiverProgressSummary.completedActivitiesCount").value(1))
                .andExpect(jsonPath("$.caregiverProgressSummary.distinctFromClinicalScore").value(true));
    }
}
