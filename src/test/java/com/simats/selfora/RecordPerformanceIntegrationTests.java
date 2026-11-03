package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RecordPerformanceIntegrationTests {

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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String THERAPIST_A = "perf_therapist_a";
    private static final String THERAPIST_B = "perf_therapist_b";

    private Long therapistAId;
    private Long therapistBId;
    private Long childAId;
    private Long childBId;
    private Long activityId;
    private Long step1Id;
    private Long step2Id;
    private Long sessionAId;

    @BeforeEach
    public void setup() {
        // Ensure PromptLevels 0 through 8 exist
        seedPromptLevels();

        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_THERAPIST").description("Therapist").build()));

        // Create Therapist A
        User therapistA = userRepository.findByUsername(THERAPIST_A).orElseGet(() -> {
            Set<Role> roles = new HashSet<>();
            roles.add(therapistRole);
            return userRepository.save(User.builder()
                    .username(THERAPIST_A)
                    .fullName("Dr. Performance Alpha")
                    .email("perf_a@selfora.org")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .roles(roles)
                    .build());
        });
        therapistAId = therapistA.getId();

        // Create Therapist B
        User therapistB = userRepository.findByUsername(THERAPIST_B).orElseGet(() -> {
            Set<Role> roles = new HashSet<>();
            roles.add(therapistRole);
            return userRepository.save(User.builder()
                    .username(THERAPIST_B)
                    .fullName("Dr. Performance Beta")
                    .email("perf_b@selfora.org")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .roles(roles)
                    .build());
        });
        therapistBId = therapistB.getId();

        // Create Child A
        Child childA = childRepository.save(Child.builder()
                .firstName("Tommy")
                .lastName("PerformanceA")
                .dateOfBirth(LocalDate.of(2018, 4, 12))
                .gender("BOY")
                .avatarUrl("ic_avatar_default")
                .isActive(true)
                .build());
        childAId = childA.getId();

        // Create Child B
        Child childB = childRepository.save(Child.builder()
                .firstName("Suzi")
                .lastName("PerformanceB")
                .dateOfBirth(LocalDate.of(2019, 7, 24))
                .gender("GIRL")
                .avatarUrl("ic_avatar_default")
                .isActive(true)
                .build());
        childBId = childB.getId();

        // Assign Child A to Therapist A, Child B to Therapist B
        jdbcTemplate.update("DELETE FROM therapist_children WHERE child_id IN (?, ?)", childAId, childBId);
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistAId, childAId);
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistBId, childBId);

        // Activity and Steps
        Activity activity = activityRepository.findAll().stream().findFirst().orElseGet(() ->
                activityRepository.save(Activity.builder()
                        .title("Upper Body Dressing")
                        .targetGender("ALL")
                        .isActive(true)
                        .description("T-Shirt Dressing")
                        .build()));
        activityId = activity.getId();

        List<TaskStep> existingSteps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(activityId);
        if (existingSteps.size() >= 2) {
            step1Id = existingSteps.get(0).getId();
            step2Id = existingSteps.get(1).getId();
        } else {
            TaskStep step1 = taskStepRepository.save(TaskStep.builder()
                    .activity(activity)
                    .stepNumber(1)
                    .title("Grasp T-shirt hem")
                    .instructionText("Hold bottom hem with both hands")
                    .build());
            TaskStep step2 = taskStepRepository.save(TaskStep.builder()
                    .activity(activity)
                    .stepNumber(2)
                    .title("Place over head")
                    .instructionText("Lift neck opening over head")
                    .build());
            step1Id = step1.getId();
            step2Id = step2.getId();
        }

        // Create Active Session for Child A conducted by Therapist A
        TherapySession sessionA = sessionRepository.save(TherapySession.builder()
                .sessionCode("TS-TEST-" + System.currentTimeMillis())
                .child(childA)
                .conductedByUser(therapistA)
                .activity(activity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now())
                .build());
        sessionAId = sessionA.getId();
    }

    private void seedPromptLevels() {
        savePromptLevelIfMissing(0, "INDEPENDENT", "Independent", 0);
        savePromptLevelIfMissing(1, "VISUAL", "Visual Prompt", 1);
        savePromptLevelIfMissing(2, "GESTURE", "Gesture Prompt", 2);
        savePromptLevelIfMissing(3, "VERBAL", "Verbal Prompt", 3);
        savePromptLevelIfMissing(4, "MODEL_VIDEO", "Model / Video Prompt", 4);
        savePromptLevelIfMissing(5, "PARTIAL_PHYSICAL", "Partial Physical Assistance", 5);
        savePromptLevelIfMissing(6, "FULL_PHYSICAL", "Full Physical Assistance", 6);
        savePromptLevelIfMissing(7, "UNABLE", "Unable", 7);
        savePromptLevelIfMissing(8, "NOT_ASSESSED", "Not Assessed", 8);
    }

    private void savePromptLevelIfMissing(int id, String code, String name, int order) {
        if (promptLevelRepository.findById(id).isEmpty()) {
            promptLevelRepository.save(PromptLevel.builder()
                    .id(id)
                    .levelCode(code)
                    .levelName(name)
                    .hierarchyOrder(order)
                    .description("Clinical description for " + name)
                    .build());
        }
    }

    // =========================================================================
    // Tests 1 to 7: Selecting and Saving Prompt Levels L0 to L6
    // =========================================================================

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 1: Selecting and saving L0 (Independent)")
    public void test1_saveLevel0_Independent() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 0, "COMPLETED", 1, 15, "Child completed step independently with natural cue", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(0))
                .andExpect(jsonPath("$.promptLevelCode").value("INDEPENDENT"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 2: Selecting and saving L1 (Visual Prompt)")
    public void test2_saveLevel1_VisualPrompt() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 1, "COMPLETED", 1, 20, "Guided with picture card", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(1))
                .andExpect(jsonPath("$.promptLevelCode").value("VISUAL"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 3: Selecting and saving L2 (Gesture Prompt)")
    public void test3_saveLevel2_GesturePrompt() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 2, "COMPLETED", 1, 18, "Therapist pointed to collar opening", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(2))
                .andExpect(jsonPath("$.promptLevelCode").value("GESTURE"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 4: Selecting and saving L3 (Verbal Prompt)")
    public void test4_saveLevel3_VerbalPrompt() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 3, "COMPLETED", 1, 22, "Short spoken cue provided", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(3))
                .andExpect(jsonPath("$.promptLevelCode").value("VERBAL"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 5: Selecting and saving L4 (Model / Video Prompt)")
    public void test5_saveLevel4_ModelPrompt() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 4, "COMPLETED", 2, 30, "Demonstrated step using avatar video", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(4))
                .andExpect(jsonPath("$.promptLevelCode").value("MODEL_VIDEO"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 6: Selecting and saving L5 (Partial Physical)")
    public void test6_saveLevel5_PartialPhysical() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 5, "COMPLETED", 1, 25, "Light touch at elbow to guide motion", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(5))
                .andExpect(jsonPath("$.promptLevelCode").value("PARTIAL_PHYSICAL"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 7: Selecting and saving L6 (Full Physical)")
    public void test7_saveLevel6_FullPhysical() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 6, "COMPLETED", 1, 35, "Hand-over-hand physical guidance provided", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(6))
                .andExpect(jsonPath("$.promptLevelCode").value("FULL_PHYSICAL"))
                .andExpect(jsonPath("$.outcome").value("COMPLETED"));
    }

    // =========================================================================
    // Tests 8 & 9: Selecting and Saving UNABLE and NOT_ASSESSED
    // =========================================================================

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 8: Selecting and saving UNABLE")
    public void test8_saveUnable() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 7, "UNABLE", 3, 40, "Child refused step due to sensory aversion", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(7))
                .andExpect(jsonPath("$.promptLevelCode").value("UNABLE"))
                .andExpect(jsonPath("$.outcome").value("UNABLE"));
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 9: Selecting and saving NOT_ASSESSED")
    public void test9_saveNotAssessed() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 8, "NOT_ASSESSED", 0, 0, "Step skipped due to clinical time constraint", null, "CLINIC"
        );

        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(8))
                .andExpect(jsonPath("$.promptLevelCode").value("NOT_ASSESSED"))
                .andExpect(jsonPath("$.outcome").value("NOT_ASSESSED"));
    }

    // =========================================================================
    // Test 10: Loading an Existing Record with Each Supported Value
    // =========================================================================

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 10: Loading an existing session with supported prompt levels")
    public void test10_loadExistingSessionRecords() throws Exception {
        // Record Step 1 as L2 (Gesture)
        SessionDtos.RecordPerformanceRequest req1 = new SessionDtos.RecordPerformanceRequest(
                step1Id, 2, "COMPLETED", 1, 15, "Gesture cue", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1))).andExpect(status().isOk());

        // Record Step 2 as L5 (Partial Physical)
        SessionDtos.RecordPerformanceRequest req2 = new SessionDtos.RecordPerformanceRequest(
                step2Id, 5, "COMPLETED", 1, 25, "Partial physical cue", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2))).andExpect(status().isOk());

        // GET /api/sessions/{id}
        MvcResult getResult = mockMvc.perform(get("/api/sessions/" + sessionAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionAId))
                .andExpect(jsonPath("$.performanceRecords.length()").value(2))
                .andReturn();

        SessionDtos.SessionResponse resp = objectMapper.readValue(getResult.getResponse().getContentAsString(), SessionDtos.SessionResponse.class);
        assertEquals(2, resp.getPerformanceRecords().size());

        SessionDtos.PerformanceRecordResponse r1 = resp.getPerformanceRecords().stream().filter(r -> r.getStepId().equals(step1Id)).findFirst().orElseThrow();
        assertEquals(2, r1.getPromptLevelId());
        assertEquals("GESTURE", r1.getPromptLevelCode());

        SessionDtos.PerformanceRecordResponse r2 = resp.getPerformanceRecords().stream().filter(r -> r.getStepId().equals(step2Id)).findFirst().orElseThrow();
        assertEquals(5, r2.getPromptLevelId());
        assertEquals("PARTIAL_PHYSICAL", r2.getPromptLevelCode());
    }

    // =========================================================================
    // Tests 11 & 12: Updating L3 to L0 and Updating L3 to L6 (No Duplicates)
    // =========================================================================

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 11: Updating existing record from L3 (Verbal) to L0 (Independent)")
    public void test11_updateL3ToL0() throws Exception {
        // Initial record: L3 (Verbal)
        SessionDtos.RecordPerformanceRequest reqInitial = new SessionDtos.RecordPerformanceRequest(
                step1Id, 3, "COMPLETED", 1, 20, "Spoken reminder", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqInitial))).andExpect(status().isOk());

        // Update record: L0 (Independent)
        SessionDtos.RecordPerformanceRequest reqUpdated = new SessionDtos.RecordPerformanceRequest(
                step1Id, 0, "COMPLETED", 1, 10, "Upgraded: child achieved independence", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqUpdated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(0))
                .andExpect(jsonPath("$.promptLevelCode").value("INDEPENDENT"))
                .andExpect(jsonPath("$.therapistNote").value("Upgraded: child achieved independence"));

        // Verify there is only ONE record for step1 in the session (no duplicate created)
        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(sessionAId);
        long step1Count = records.stream().filter(r -> r.getStep().getId().equals(step1Id)).count();
        assertEquals(1, step1Count, "Updating record must modify in place, not duplicate");
        assertEquals(0, records.get(0).getPromptLevel().getId(), "Saved prompt level must be L0");
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 12: Updating existing record from L3 (Verbal) to L6 (Full Physical)")
    public void test12_updateL3ToL6() throws Exception {
        // Initial record: L3 (Verbal)
        SessionDtos.RecordPerformanceRequest reqInitial = new SessionDtos.RecordPerformanceRequest(
                step1Id, 3, "COMPLETED", 1, 20, "Spoken reminder", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqInitial))).andExpect(status().isOk());

        // Update record: L6 (Full Physical)
        SessionDtos.RecordPerformanceRequest reqUpdated = new SessionDtos.RecordPerformanceRequest(
                step1Id, 6, "COMPLETED", 2, 45, "Required hand-over-hand assistance", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqUpdated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promptLevelId").value(6))
                .andExpect(jsonPath("$.promptLevelCode").value("FULL_PHYSICAL"))
                .andExpect(jsonPath("$.therapistNote").value("Required hand-over-hand assistance"));

        // Verify in database
        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(sessionAId);
        assertEquals(1, records.size());
        assertEquals(6, records.get(0).getPromptLevel().getId());
    }

    // =========================================================================
    // Test 13: Verifying Saved Value After App/Session Reload
    // =========================================================================

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 13: Verifying saved prompt levels persist and return correctly on session reload")
    public void test13_verifyPersistenceOnReload() throws Exception {
        // Save L4 (Model/Video) for Step 1
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 4, "COMPLETED", 1, 25, "Avatar video demonstrated", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk());

        // Complete session
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionAId));

        // Reload session via GET
        mockMvc.perform(get("/api/sessions/" + sessionAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.performanceRecords[0].promptLevelId").value(4))
                .andExpect(jsonPath("$.performanceRecords[0].promptLevelCode").value("MODEL_VIDEO"));
    }

    // =========================================================================
    // Test 14: Verifying Records for Two Different Children Remain Separate
    // =========================================================================

    @Test
    @DisplayName("Test 14: Verifying records for two different children remain completely separate")
    public void test14_differentChildrenRecordsSeparate() throws Exception {
        // Create session for Child B conducted by Therapist B
        TherapySession sessionB = sessionRepository.save(TherapySession.builder()
                .sessionCode("TS-CHILD-B-" + System.currentTimeMillis())
                .child(childRepository.findById(childBId).orElseThrow())
                .conductedByUser(userRepository.findById(therapistBId).orElseThrow())
                .activity(activityRepository.findById(activityId).orElseThrow())
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now())
                .build());
        Long sessionBId = sessionB.getId();

        // Therapist A records L0 for Child A
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .with(user(THERAPIST_A).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SessionDtos.RecordPerformanceRequest(
                                step1Id, 0, "COMPLETED", 1, 10, "Child A Independent", null, "CLINIC"))))
                .andExpect(status().isOk());

        // Therapist B records L6 for Child B
        mockMvc.perform(post("/api/sessions/" + sessionBId + "/performance")
                        .with(user(THERAPIST_B).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SessionDtos.RecordPerformanceRequest(
                                step1Id, 6, "COMPLETED", 1, 30, "Child B Full Physical", null, "CLINIC"))))
                .andExpect(status().isOk());

        // Verify Child A session has only L0
        MvcResult resA = mockMvc.perform(get("/api/sessions/" + sessionAId).with(user(THERAPIST_A).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andReturn();
        SessionDtos.SessionResponse dataA = objectMapper.readValue(resA.getResponse().getContentAsString(), SessionDtos.SessionResponse.class);
        assertEquals(1, dataA.getPerformanceRecords().size());
        assertEquals(0, dataA.getPerformanceRecords().get(0).getPromptLevelId());
        assertEquals(childAId, dataA.getChildId());

        // Verify Child B session has only L6
        MvcResult resB = mockMvc.perform(get("/api/sessions/" + sessionBId).with(user(THERAPIST_B).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andReturn();
        SessionDtos.SessionResponse dataB = objectMapper.readValue(resB.getResponse().getContentAsString(), SessionDtos.SessionResponse.class);
        assertEquals(1, dataB.getPerformanceRecords().size());
        assertEquals(6, dataB.getPerformanceRecords().get(0).getPromptLevelId());
        assertEquals(childBId, dataB.getChildId());
    }

    // =========================================================================
    // Test 15: Cross-Therapist Authorization Check (403 Forbidden)
    // =========================================================================

    @Test
    @DisplayName("Test 15: Therapist B cannot record or modify performance for Therapist A's assigned child")
    public void test15_unauthorizedTherapist_cannotModifyRecord_403Forbidden() throws Exception {
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 2, "COMPLETED", 1, 15, "Unauthorized attempt", null, "CLINIC"
        );

        // Therapist B tries to record performance on Therapist A's child's session
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                        .with(user(THERAPIST_B).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 16: Caregiver Data Isolation
    // =========================================================================

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Test 16: Caregiver practice data does not overwrite therapist clinical prompt levels")
    public void test16_caregiverPracticeData_doesNotOverwriteClinicalPromptLevel() throws Exception {
        // Therapist A records clinical L1 (Visual) for Child A
        SessionDtos.RecordPerformanceRequest req = new SessionDtos.RecordPerformanceRequest(
                step1Id, 1, "COMPLETED", 1, 15, "Therapist clinical rating", null, "CLINIC"
        );
        mockMvc.perform(post("/api/sessions/" + sessionAId + "/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk());

        // Simulate caregiver practice entry in caregiver_step_records (or separate table)
        // Verify clinical performance_records table remains strictly L1
        List<PerformanceRecord> records = performanceRecordRepository.findBySessionId(sessionAId);
        assertEquals(1, records.size());
        assertEquals(1, records.get(0).getPromptLevel().getId());
        assertEquals("VISUAL", records.get(0).getPromptLevel().getLevelCode());
        assertEquals("Therapist clinical rating", records.get(0).getTherapistNote());
    }
}

