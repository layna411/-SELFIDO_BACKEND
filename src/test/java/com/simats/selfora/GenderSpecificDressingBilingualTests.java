package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.ChildModeDtos;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GenderSpecificDressingBilingualTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskStepRepository taskStepRepository;

    // Child 13 is BOY, Child 14 is GIRL. Both are linked to caregiver1
    private static final Long BOY_CHILD_ID = 13L;
    private static final Long GIRL_CHILD_ID = 14L;

    // =========================================================================
    // TEST 1: BOY CHILD DRESSING PATHWAY SCOPING
    // =========================================================================
    @Test
    @DisplayName("Test 1: Boy Child sees ONLY Boys Dressing + Common Activities (0 Girls Dressing)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void boyChildDressingScoping() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/child-mode/dashboard/" + BOY_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(BOY_CHILD_ID))
                .andExpect(jsonPath("$.gender").value("BOY"))
                .andReturn();

        ChildModeDtos.ChildDashboardResponse dashboard = objectMapper.readValue(
                result.getResponse().getContentAsString(), ChildModeDtos.ChildDashboardResponse.class);

        assertNotNull(dashboard.getAllActivities());
        List<Long> activityIds = dashboard.getAllActivities().stream()
                .map(ChildModeDtos.ChildActivitySummaryDto::getActivityId)
                .collect(Collectors.toList());

        // Approved Boys Dressing Activities: 1 (T-Shirt), 2 (Shirt), 3 (Zip-Up Jacket), 4 (Vest),
        // 5 (Trousers), 6 (Jeans), 7 (Elastic Shorts), 16 (Briefs)
        List<Long> expectedBoysDressing = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 16L);
        for (Long expectedId : expectedBoysDressing) {
            assertTrue(activityIds.contains(expectedId), "Boy dashboard must contain Boy Dressing activity ID: " + expectedId);
        }

        // Girls Dressing Activities: 17, 18, 19, 20 (Frock), 21 (Camisole), 22 (Skirt), 23, 24, 25 (Panties)
        List<Long> forbiddenGirlsDressing = Arrays.asList(17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L);
        for (Long forbiddenId : forbiddenGirlsDressing) {
            assertFalse(activityIds.contains(forbiddenId), "Boy dashboard must NOT contain Girl Dressing activity ID: " + forbiddenId);
        }

        // Total available for Boys = 8 dressing + 5 footwear + 3 eating = 16 activities
        assertEquals(16, dashboard.getAllActivities().size(), "Boy child must have exactly 16 available approved activities");
    }

    // =========================================================================
    // TEST 2: GIRL CHILD DRESSING PATHWAY SCOPING
    // =========================================================================
    @Test
    @DisplayName("Test 2: Girl Child sees ONLY Girls Dressing + Common Activities (0 Boys Dressing)")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void girlChildDressingScoping() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/child-mode/dashboard/" + GIRL_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(GIRL_CHILD_ID))
                .andExpect(jsonPath("$.gender").value("GIRL"))
                .andReturn();

        ChildModeDtos.ChildDashboardResponse dashboard = objectMapper.readValue(
                result.getResponse().getContentAsString(), ChildModeDtos.ChildDashboardResponse.class);

        assertNotNull(dashboard.getAllActivities());
        List<Long> activityIds = dashboard.getAllActivities().stream()
                .map(ChildModeDtos.ChildActivitySummaryDto::getActivityId)
                .collect(Collectors.toList());

        // Approved Girls Dressing Activities:
        // Upper: 17 (T-Shirt), 18 (Front-Open Top), 19 (Zip-Up Jacket), 20 (Pullover Frock), 21 (Camisole)
        // Lower: 22 (Drawstring Skirt), 23 (Elastic Shorts), 24 (Jeans with Zipper), 25 (Panties/Briefs)
        List<Long> expectedGirlsDressing = Arrays.asList(17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L);
        for (Long expectedId : expectedGirlsDressing) {
            assertTrue(activityIds.contains(expectedId), "Girl dashboard must contain Girl Dressing activity ID: " + expectedId);
        }

        // Boys Dressing Activities: 1, 2, 3, 4, 5, 6, 7, 16
        List<Long> forbiddenBoysDressing = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 16L);
        for (Long forbiddenId : forbiddenBoysDressing) {
            assertFalse(activityIds.contains(forbiddenId), "Girl dashboard must NOT contain Boy Dressing activity ID: " + forbiddenId);
        }

        // Total available for Girls = 9 dressing + 5 footwear + 3 eating = 17 activities
        assertEquals(17, dashboard.getAllActivities().size(), "Girl child must have exactly 17 available approved activities");
    }

    // =========================================================================
    // TEST 3: COMMON FOOTWEAR AND EATING ACTIVITIES ACCESSIBLE BY BOTH GENDERS
    // =========================================================================
    @Test
    @DisplayName("Test 3: Footwear and Eating activities are accessible to BOTH Boys and Girls")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void commonActivitiesAccessibleByBothGenders() throws Exception {
        List<Long> commonIds = Arrays.asList(
                8L,  // Wearing Socks
                9L,  // Velcro Shoes
                10L, // Shoes with Shoelaces
                26L, // Slip-On Shoes
                27L, // Shoelace Tying
                11L, // Eating Rice with Gravy/Curry
                12L, // Eating Breakfast Items with Hands
                13L  // Eating with Spoon
        );

        // Check Boy Child
        MvcResult boyResult = mockMvc.perform(get("/api/child-mode/dashboard/" + BOY_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();
        ChildModeDtos.ChildDashboardResponse boyDashboard = objectMapper.readValue(
                boyResult.getResponse().getContentAsString(), ChildModeDtos.ChildDashboardResponse.class);
        List<Long> boyActivityIds = boyDashboard.getAllActivities().stream()
                .map(ChildModeDtos.ChildActivitySummaryDto::getActivityId)
                .collect(Collectors.toList());

        for (Long commonId : commonIds) {
            assertTrue(boyActivityIds.contains(commonId), "Boy child must have common activity ID: " + commonId);
        }

        // Check Girl Child
        MvcResult girlResult = mockMvc.perform(get("/api/child-mode/dashboard/" + GIRL_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();
        ChildModeDtos.ChildDashboardResponse girlDashboard = objectMapper.readValue(
                girlResult.getResponse().getContentAsString(), ChildModeDtos.ChildDashboardResponse.class);
        List<Long> girlActivityIds = girlDashboard.getAllActivities().stream()
                .map(ChildModeDtos.ChildActivitySummaryDto::getActivityId)
                .collect(Collectors.toList());

        for (Long commonId : commonIds) {
            assertTrue(girlActivityIds.contains(commonId), "Girl child must have common activity ID: " + commonId);
        }
    }

    // =========================================================================
    // TEST 4: INACTIVE GROOMING ACTIVITIES EXCLUDED FROM VIEWS
    // =========================================================================
    @Test
    @DisplayName("Test 4: Inactive Grooming & Handwashing (IDs 14, 15) are strictly excluded from Child Mode")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void groomingActivitiesExcluded() throws Exception {
        MvcResult boyResult = mockMvc.perform(get("/api/child-mode/dashboard/" + BOY_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();
        ChildModeDtos.ChildDashboardResponse boyDashboard = objectMapper.readValue(
                boyResult.getResponse().getContentAsString(), ChildModeDtos.ChildDashboardResponse.class);
        List<Long> boyIds = boyDashboard.getAllActivities().stream()
                .map(ChildModeDtos.ChildActivitySummaryDto::getActivityId)
                .collect(Collectors.toList());

        assertFalse(boyIds.contains(14L), "Activity 14 (Toothbrushing) must NOT appear in child mode dashboard");
        assertFalse(boyIds.contains(15L), "Activity 15 (Handwashing) must NOT appear in child mode dashboard");

        MvcResult girlResult = mockMvc.perform(get("/api/child-mode/dashboard/" + GIRL_CHILD_ID))
                .andExpect(status().isOk())
                .andReturn();
        ChildModeDtos.ChildDashboardResponse girlDashboard = objectMapper.readValue(
                girlResult.getResponse().getContentAsString(), ChildModeDtos.ChildDashboardResponse.class);
        List<Long> girlIds = girlDashboard.getAllActivities().stream()
                .map(ChildModeDtos.ChildActivitySummaryDto::getActivityId)
                .collect(Collectors.toList());

        assertFalse(girlIds.contains(14L), "Activity 14 (Toothbrushing) must NOT appear in girl dashboard");
        assertFalse(girlIds.contains(15L), "Activity 15 (Handwashing) must NOT appear in girl dashboard");
    }

    // =========================================================================
    // TEST 5: FULL BILINGUAL CONTENT & STARTING POSITIONS ACROSS ALL 25 ACTIVITIES
    // =========================================================================
    @Test
    @DisplayName("Test 5: All 25 Approved Activities have Starting Positions and Tamil Translations")
    void bilingualContentIntegrity() {
        List<Long> approvedActivityIds = Arrays.asList(
                1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L,
                16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L
        );

        for (Long actId : approvedActivityIds) {
            Activity act = activityRepository.findById(actId).orElse(null);
            assertNotNull(act, "Activity " + actId + " must exist in database");
            assertTrue(act.getIsActive(), "Activity " + actId + " must be active");

            // English & Tamil titles
            assertNotNull(act.getTitle(), "Activity " + actId + " must have English title");
            assertNotNull(act.getTitleTa(), "Activity " + actId + " must have Tamil title");
            assertFalse(act.getTitleTa().trim().isEmpty(), "Activity " + actId + " Tamil title cannot be empty");

            // English & Tamil starting positions
            assertNotNull(act.getStartingPosition(), "Activity " + actId + " must have English starting position");
            assertNotNull(act.getStartingPositionTa(), "Activity " + actId + " must have Tamil starting position");
            assertFalse(act.getStartingPositionTa().trim().isEmpty(), "Activity " + actId + " Tamil starting position cannot be empty");

            // Task steps
            List<TaskStep> steps = taskStepRepository.findByActivityIdOrderByStepNumberAsc(actId);
            assertFalse(steps.isEmpty(), "Activity " + actId + " must have task steps");

            for (TaskStep step : steps) {
                assertNotNull(step.getTitle(), "Step " + step.getId() + " must have English title");
                assertNotNull(step.getTitleTa(), "Step " + step.getId() + " must have Tamil title");
                assertNotNull(step.getChildInstruction(), "Step " + step.getId() + " must have English child instruction");
                assertNotNull(step.getChildInstructionTa(), "Step " + step.getId() + " must have Tamil child instruction");
                assertFalse(step.getChildInstructionTa().trim().isEmpty(), "Step " + step.getId() + " Tamil child instruction cannot be empty");
            }
        }
    }

    // =========================================================================
    // TEST 6: LANGUAGE SWITCHING DOES NOT RESET PROGRESS, STARS, OR ACTIVITY IDS
    // =========================================================================
    @Test
    @DisplayName("Test 6: Language switching preserves Activity IDs, progress, and stars")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void languageSwitchingPreservesProgress() throws Exception {
        // Step 1: Practice Activity 1 (T-Shirt) for boyChild
        ChildModeDtos.RecordStepProgressRequest req1 = new ChildModeDtos.RecordStepProgressRequest();
        req1.setChildId(BOY_CHILD_ID);
        req1.setActivityId(1L);
        req1.setStepNumber(1);
        req1.setIsActivityCompleted(false);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId").value(1L))
                .andExpect(jsonPath("$.lastStepNumber").value(1));

        // Step 2: Complete Activity 1
        ChildModeDtos.RecordStepProgressRequest reqComplete = new ChildModeDtos.RecordStepProgressRequest();
        reqComplete.setChildId(BOY_CHILD_ID);
        reqComplete.setActivityId(1L);
        reqComplete.setStepNumber(20);
        reqComplete.setIsActivityCompleted(true);

        mockMvc.perform(post("/api/child-mode/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqComplete)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true));

        // Step 3: Switch language preference to Tamil (TA)
        ChildModeDtos.ChildModePreferenceDto prefUpdate = new ChildModeDtos.ChildModePreferenceDto();
        prefUpdate.setPreferredName("Aarav");
        prefUpdate.setLanguage("TA");
        prefUpdate.setAudioEnabled(true);
        prefUpdate.setAnimationEnabled(true);

        mockMvc.perform(put("/api/child-mode/preferences/" + BOY_CHILD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("TA"));

        // Step 4: Verify Dashboard returns Tamil titles while keeping the exact same progress & stars
        MvcResult res = mockMvc.perform(get("/api/child-mode/dashboard/" + BOY_CHILD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.language").value("TA"))
                .andReturn();

        ChildModeDtos.ChildDashboardResponse dashboard = objectMapper.readValue(
                res.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8), ChildModeDtos.ChildDashboardResponse.class);

        ChildModeDtos.ChildActivitySummaryDto act1 = dashboard.getAllActivities().stream()
                .filter(a -> a.getActivityId() == 1L)
                .findFirst()
                .orElse(null);

        assertNotNull(act1);
        assertTrue(act1.isCompleted());
        assertEquals("T-Shirt", act1.getTitle());
        assertNotNull(act1.getTitleTa());
        assertFalse(act1.getTitleTa().trim().isEmpty());
        assertNotNull(act1.getStartingPositionTa());
    }

    // =========================================================================
    // TEST 7: GET /api/activities?gender=BOY and ?gender=GIRL DIRECT QUERY
    // =========================================================================
    @Test
    @DisplayName("Test 7: Direct GET /api/activities?gender=BOY / GIRL endpoint filtering")
    @WithMockUser(username = "caregiver1", roles = {"CAREGIVER"})
    void directActivityGenderFiltering() throws Exception {
        // Test BOY filter
        mockMvc.perform(get("/api/activities?gender=BOY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(16)))
                .andExpect(jsonPath("$[*].id", hasItem(1)))
                .andExpect(jsonPath("$[*].id", hasItem(5)))
                .andExpect(jsonPath("$[*].id", not(hasItem(20)))) // No Frock
                .andExpect(jsonPath("$[*].id", not(hasItem(14)))); // No Toothbrushing

        // Test GIRL filter
        mockMvc.perform(get("/api/activities?gender=GIRL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(17)))
                .andExpect(jsonPath("$[*].id", hasItem(20))) // Frock
                .andExpect(jsonPath("$[*].id", hasItem(22))) // Skirt
                .andExpect(jsonPath("$[*].id", not(hasItem(1)))) // No Boy T-Shirt
                .andExpect(jsonPath("$[*].id", not(hasItem(14)))); // No Toothbrushing
    }
}