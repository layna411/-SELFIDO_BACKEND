package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.AdminDtos;
import com.simats.selfora.dto.ChildDto;
import com.simats.selfora.dto.TherapistDtos;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.Role;
import com.simats.selfora.entity.User;
import com.simats.selfora.repository.ChildRepository;
import com.simats.selfora.repository.RoleRepository;
import com.simats.selfora.repository.UserRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TherapistChildScopingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long therapistAId;
    private Long therapistBId;
    private Long therapistEmptyId;
    private Long childAId;
    private Long childBId;

    private static final String THERAPIST_A = "scoping_therapist_a";
    private static final String THERAPIST_B = "scoping_therapist_b";
    private static final String THERAPIST_EMPTY = "scoping_therapist_empty";
    private static final String SUPER_ADMIN = "admin";

    @BeforeEach
    public void setupTestData() {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_THERAPIST").description("Therapist").build()));

        // Create Therapist A
        User therapistA = userRepository.findByUsername(THERAPIST_A).orElseGet(() -> {
            Set<Role> roles = new HashSet<>();
            roles.add(therapistRole);
            return userRepository.save(User.builder()
                    .username(THERAPIST_A)
                    .fullName("Dr. Therapist Alpha")
                    .email("therapist_a@selfora.org")
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
                    .fullName("Dr. Therapist Beta")
                    .email("therapist_b@selfora.org")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .roles(roles)
                    .build());
        });
        therapistBId = therapistB.getId();

        // Create Empty Therapist (no assigned children)
        User therapistEmpty = userRepository.findByUsername(THERAPIST_EMPTY).orElseGet(() -> {
            Set<Role> roles = new HashSet<>();
            roles.add(therapistRole);
            return userRepository.save(User.builder()
                    .username(THERAPIST_EMPTY)
                    .fullName("Dr. Empty Therapist")
                    .email("therapist_empty@selfora.org")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .isActive(true)
                    .roles(roles)
                    .build());
        });
        therapistEmptyId = therapistEmpty.getId();

        // Create Child A
        Child childA = childRepository.save(Child.builder()
                .firstName("AlphaChild")
                .lastName("Patient")
                .dateOfBirth(LocalDate.of(2018, 5, 10))
                .gender("BOY")
                .avatarUrl("ic_avatar_default")
                .isActive(true)
                .diagnosisNotes("Scoping Test Patient A")
                .build());
        childAId = childA.getId();

        // Create Child B
        Child childB = childRepository.save(Child.builder()
                .firstName("BetaChild")
                .lastName("Patient")
                .dateOfBirth(LocalDate.of(2019, 8, 20))
                .gender("GIRL")
                .avatarUrl("ic_avatar_default")
                .isActive(true)
                .diagnosisNotes("Scoping Test Patient B")
                .build());
        childBId = childB.getId();

        // Assign Child A to Therapist A
        jdbcTemplate.update("DELETE FROM therapist_children WHERE child_id IN (?, ?)", childAId, childBId);
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistAId, childAId);

        // Assign Child B to Therapist B
        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistBId, childBId);
    }

    // =========================================================================
    // 1. Data Isolation: Therapist A vs Therapist B in /api/therapist/children
    // =========================================================================
    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A sees only Child A in GET /api/therapist/children")
    public void therapistA_seesOnlyAssignedChildren_inTherapistChildren() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/therapist/children"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        TherapistDtos.ChildSummaryItem[] children = objectMapper.readValue(json, TherapistDtos.ChildSummaryItem[].class);

        assertTrue(Arrays.stream(children).anyMatch(c -> c.getId().equals(childAId)), "Therapist A must see assigned Child A");
        assertFalse(Arrays.stream(children).anyMatch(c -> c.getId().equals(childBId)), "Therapist A must NEVER see Therapist B's Child B");
    }

    @Test
    @WithMockUser(username = THERAPIST_B, roles = {"THERAPIST"})
    @DisplayName("Therapist B sees only Child B in GET /api/therapist/children")
    public void therapistB_seesOnlyAssignedChildren_inTherapistChildren() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/therapist/children"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        TherapistDtos.ChildSummaryItem[] children = objectMapper.readValue(json, TherapistDtos.ChildSummaryItem[].class);

        assertTrue(Arrays.stream(children).anyMatch(c -> c.getId().equals(childBId)), "Therapist B must see assigned Child B");
        assertFalse(Arrays.stream(children).anyMatch(c -> c.getId().equals(childAId)), "Therapist B must NEVER see Therapist A's Child A");
    }

    // =========================================================================
    // 2. Data Isolation in GET /api/children
    // =========================================================================
    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A sees only Child A in GET /api/children")
    public void therapistA_seesOnlyAssignedChildren_inChildController() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/children"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ChildDto[] children = objectMapper.readValue(json, ChildDto[].class);

        assertTrue(Arrays.stream(children).anyMatch(c -> c.getId().equals(childAId)), "Therapist A must see assigned Child A");
        assertFalse(Arrays.stream(children).anyMatch(c -> c.getId().equals(childBId)), "Therapist A must NEVER see Child B");
    }

    // =========================================================================
    // 3. Empty Therapist: Returns Clean Empty List (not error, not mock data)
    // =========================================================================
    @Test
    @WithMockUser(username = THERAPIST_EMPTY, roles = {"THERAPIST"})
    @DisplayName("Unassigned Therapist sees empty array in /api/therapist/children and /api/children")
    public void emptyTherapist_seesEmptyArray_notError() throws Exception {
        MvcResult res1 = mockMvc.perform(get("/api/therapist/children"))
                .andExpect(status().isOk())
                .andReturn();
        TherapistDtos.ChildSummaryItem[] thChildren = objectMapper.readValue(res1.getResponse().getContentAsString(), TherapistDtos.ChildSummaryItem[].class);
        assertEquals(0, thChildren.length, "Unassigned therapist must receive 0 children in /api/therapist/children");

        MvcResult res2 = mockMvc.perform(get("/api/children"))
                .andExpect(status().isOk())
                .andReturn();
        ChildDto[] myChildren = objectMapper.readValue(res2.getResponse().getContentAsString(), ChildDto[].class);
        assertEquals(0, myChildren.length, "Unassigned therapist must receive 0 children in /api/children");
    }

    // =========================================================================
    // 4. Dashboard Summary Metrics Scoping
    // =========================================================================
    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A Dashboard Summary matches only assigned children")
    public void therapistA_dashboardStats_scopedCorrectly() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/therapist/dashboard-summary"))
                .andExpect(status().isOk())
                .andReturn();

        TherapistDtos.TherapistDashboardSummaryResponse summary = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TherapistDtos.TherapistDashboardSummaryResponse.class
        );

        assertTrue(summary.getActiveChildrenCount() >= 1, "Therapist A activeChildrenCount must include Child A");
        assertEquals("Dr. Therapist Alpha", summary.getTherapistName());
    }

    @Test
    @WithMockUser(username = THERAPIST_EMPTY, roles = {"THERAPIST"})
    @DisplayName("Empty Therapist Dashboard Summary returns 0 for active children")
    public void emptyTherapist_dashboardStats_returnsZeroChildren() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/therapist/dashboard-summary"))
                .andExpect(status().isOk())
                .andReturn();

        TherapistDtos.TherapistDashboardSummaryResponse summary = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TherapistDtos.TherapistDashboardSummaryResponse.class
        );

        assertEquals(0, summary.getActiveChildrenCount(), "Empty therapist activeChildrenCount must be exactly 0");
        assertEquals(0, summary.getTodaySessionsCount(), "Empty therapist todaySessionsCount must be 0");
        assertEquals(0, summary.getPendingReviewsCount(), "Empty therapist pendingReviewsCount must be 0");
        assertEquals(0, summary.getTodaySchedule().size(), "Empty therapist schedule must be empty");
    }

    @Test
    @WithMockUser(username = SUPER_ADMIN, roles = {"SUPER_ADMIN", "ADMIN"})
    @DisplayName("Super Admin Dashboard Summary aggregates across all children")
    public void superAdmin_dashboardStats_reflectsSystemWide() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/therapist/dashboard-summary"))
                .andExpect(status().isOk())
                .andReturn();

        TherapistDtos.TherapistDashboardSummaryResponse summary = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TherapistDtos.TherapistDashboardSummaryResponse.class
        );

        assertTrue(summary.getActiveChildrenCount() >= 2, "Super Admin activeChildrenCount must include all children system-wide");
    }

    // =========================================================================
    // 5. Cross-Therapist Access Rejection (403 Forbidden)
    // =========================================================================
    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B profile returns 403 Forbidden")
    public void therapistA_accessingChildBProfile_forbidden() throws Exception {
        mockMvc.perform(get("/api/children/" + childBId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A validating access for Child B returns 403 Forbidden")
    public void therapistA_validatingChildBAccess_forbidden() throws Exception {
        mockMvc.perform(get("/api/children/" + childBId + "/validate-access"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B assessments returns 403 Forbidden")
    public void therapistA_accessingChildBAssessments_forbidden() throws Exception {
        mockMvc.perform(get("/api/assessments/child/" + childBId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B therapy sessions returns 403 Forbidden")
    public void therapistA_accessingChildBSessions_forbidden() throws Exception {
        mockMvc.perform(get("/api/sessions/child/" + childBId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B home programs returns 403 Forbidden")
    public void therapistA_accessingChildBHomePrograms_forbidden() throws Exception {
        mockMvc.perform(get("/api/home-programs/child/" + childBId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B progress analytics returns 403 Forbidden")
    public void therapistA_accessingChildBProgress_forbidden() throws Exception {
        mockMvc.perform(get("/api/children/" + childBId + "/progress"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B PDF report returns 403 Forbidden")
    public void therapistA_accessingChildBReportPdf_forbidden() throws Exception {
        mockMvc.perform(get("/api/reports/children/" + childBId + "/pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = THERAPIST_A, roles = {"THERAPIST"})
    @DisplayName("Therapist A accessing Child B messages returns 403 Forbidden")
    public void therapistA_accessingChildBMessages_forbidden() throws Exception {
        mockMvc.perform(get("/api/messages/child/" + childBId))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 6. Child Reassignment Workflow: Instant Revocation & Grant
    // =========================================================================
    @Test
    @DisplayName("Super Admin reassigns Child A to Therapist B: instant revocation & grant")
    public void reassignmentWorkflow_adminReassignsChild() throws Exception {
        // Step 1: Therapist A initially has access to Child A
        mockMvc.perform(get("/api/children/" + childAId)
                        .with(user(THERAPIST_A).roles("THERAPIST")))
                .andExpect(status().isOk());

        // Step 2: Therapist B initially DOES NOT have access to Child A
        mockMvc.perform(get("/api/children/" + childAId)
                        .with(user(THERAPIST_B).roles("THERAPIST")))
                .andExpect(status().isForbidden());

        // Step 3: Super Admin reassigns Child A to Therapist B
        AdminDtos.ChildReassignRequest req = new AdminDtos.ChildReassignRequest();
        req.setNewTherapistId(therapistBId);
        req.setReason("Caseload reallocation in clinical workflow");

        mockMvc.perform(put("/api/admin/children/" + childAId + "/reassign-therapist")
                        .with(user(SUPER_ADMIN).roles("SUPER_ADMIN", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.assignedTherapistId").value(therapistBId));

        // Step 4: Therapist A's access is revoked IMMEDIATELY (403 Forbidden)
        mockMvc.perform(get("/api/children/" + childAId)
                        .with(user(THERAPIST_A).roles("THERAPIST")))
                .andExpect(status().isForbidden());

        // Step 5: Therapist B now has access IMMEDIATELY (200 OK)
        mockMvc.perform(get("/api/children/" + childAId)
                        .with(user(THERAPIST_B).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(childAId))
                .andExpect(jsonPath("$.firstName").value("AlphaChild"));
    }
}

