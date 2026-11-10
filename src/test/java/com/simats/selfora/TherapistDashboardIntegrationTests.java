package com.simats.selfora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.CaregiverPracticeDtos;
import com.simats.selfora.dto.HomeProgramDtos;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TherapistDashboardIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ChildRepository childRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private PromptLevelRepository promptLevelRepository;
    @Autowired private TherapySessionRepository therapySessionRepository;
    @Autowired private HomeProgramRepository homeProgramRepository;
    @Autowired private CaregiverPracticeSessionRepository practiceSessionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String TEST_THERAPIST = "dash_test_therapist";
    private static final String TEST_CAREGIVER = "dash_test_caregiver";

    private Long therapistId;
    private Long caregiverId;
    private Long childId;
    private Long activityId;
    private Long homeProgramId;
    private Long practiceId;

    @BeforeEach
    void setupData() {
        Role thRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_THERAPIST").description("Therapist").build()));
        Role cgRole = roleRepository.findByName("ROLE_CAREGIVER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CAREGIVER").description("Caregiver").build()));

        User therapist = userRepository.save(User.builder()
                .username(TEST_THERAPIST)
                .fullName("Dr. Dashboard Tester")
                .email("dash_therapist@selfora.org")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(thRole))
                .isActive(true)
                .build());
        therapistId = therapist.getId();

        User caregiver = userRepository.save(User.builder()
                .username(TEST_CAREGIVER)
                .fullName("Caregiver Tester")
                .email("dash_caregiver@selfora.org")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .roles(Set.of(cgRole))
                .isActive(true)
                .build());
        caregiverId = caregiver.getId();

        Child child = childRepository.save(Child.builder()
                .firstName("Rohan")
                .lastName("Verma")
                .dateOfBirth(LocalDate.of(2020, 3, 15))
                .gender("BOY")
                .isActive(true)
                .build());
        childId = child.getId();

        jdbcTemplate.update("INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)", therapistId, childId);
        jdbcTemplate.update("INSERT INTO caregiver_children (caregiver_id, child_id, relationship) VALUES (?, ?, 'Mother')", caregiverId, childId);

        Activity activity = activityRepository.findById(1L)
                .orElseGet(() -> activityRepository.findAll().stream().findFirst().orElseThrow());
        activityId = activity.getId();

        PromptLevel promptLevel = promptLevelRepository.findById(1)
                .orElseGet(() -> promptLevelRepository.findAll().stream().findFirst().orElseThrow());

        // 1. Create a Completed Session Today and an In-Progress Session Today
        therapySessionRepository.save(TherapySession.builder()
                .sessionCode("DASH-TODAY-DONE-" + System.nanoTime())
                .child(child)
                .conductedByUser(therapist)
                .activity(activity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now().minusMinutes(10))
                .totalDurationSeconds(1200)
                .build());

        therapySessionRepository.save(TherapySession.builder()
                .sessionCode("DASH-TODAY-ACTIVE-" + System.nanoTime())
                .child(child)
                .conductedByUser(therapist)
                .activity(activity)
                .sessionType("CLINIC_THERAPY")
                .environment("CLINIC")
                .startTime(LocalDateTime.now())
                .build());

        // 2. Create 1 Active Home Program and 1 Completed Home Program (Completed should NOT count in activeHomeProgramsCount)
        HomeProgram activeHp = homeProgramRepository.save(HomeProgram.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .targetPromptLevel(promptLevel)
                .title("Evening Dressing Routine")
                .status("ACTIVE")
                .priority("HIGH")
                .scheduleType("DAILY")
                .preferredTime("EVENING")
                .frequencyPerWeek(5)
                .targetDurationMinutes(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .isActive(true)
                .build());
        homeProgramId = activeHp.getId();

        homeProgramRepository.save(HomeProgram.builder()
                .child(child)
                .therapist(therapist)
                .activity(activity)
                .targetPromptLevel(promptLevel)
                .title("Old Completed Program")
                .status("COMPLETED")
                .frequencyPerWeek(3)
                .targetDurationMinutes(10)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(1))
                .isActive(false)
                .build());

        // 3. Create 1 Pending (SUBMITTED) Caregiver Practice Session
        CaregiverPracticeSession practice = practiceSessionRepository.save(CaregiverPracticeSession.builder()
                .child(child)
                .caregiver(caregiver)
                .homeProgram(activeHp)
                .activity(activity)
                .practiceDate(LocalDate.now())
                .durationMinutes(12)
                .caregiverNotes("Completed with visual prompts cleanly.")
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .build());
        practiceId = practice.getId();
    }

    @Test
    @DisplayName("Dashboard summary returns exact Sessions Today, Home Programs, and Pending Reviews with populated lists")
    void dashboardSummaryMatchesRealRecords() throws Exception {
        mockMvc.perform(get("/api/therapist/dashboard-summary")
                        .with(user(TEST_THERAPIST).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todaySessionsCount").value(2))
                .andExpect(jsonPath("$.activeHomeProgramsCount").value(1))
                .andExpect(jsonPath("$.pendingReviewsCount").value(1))
                .andExpect(jsonPath("$.todaySchedule", hasSize(2)))
                .andExpect(jsonPath("$.todaySchedule[0].childName").value("Rohan Verma"))
                .andExpect(jsonPath("$.todaySchedule[0].adlActivity", not(emptyOrNullString())))
                .andExpect(jsonPath("$.todaySchedule[*].status", containsInAnyOrder("COMPLETED", "IN_PROGRESS")))
                .andExpect(jsonPath("$.pendingReviews", hasSize(1)))
                .andExpect(jsonPath("$.pendingReviews[0].practiceId").value(practiceId))
                .andExpect(jsonPath("$.pendingReviews[0].activityName").value("Evening Dressing Routine"))
                .andExpect(jsonPath("$.pendingReviews[0].notes").value("Completed with visual prompts cleanly."));
    }

    @Test
    @DisplayName("GET /api/therapist/sessions/today returns today's full session details")
    void getTodaySessionsEndpointReturnsDetails() throws Exception {
        mockMvc.perform(get("/api/therapist/sessions/today")
                        .with(user(TEST_THERAPIST).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].childId").value(childId))
                .andExpect(jsonPath("$[0].childName").value("Rohan Verma"))
                .andExpect(jsonPath("$[0].activityTitle", not(emptyOrNullString())));
    }

    @Test
    @DisplayName("GET /api/therapist/home-programs returns only active assigned home programs")
    void getAssignedHomeProgramsEndpointReturnsActivePrograms() throws Exception {
        mockMvc.perform(get("/api/therapist/home-programs")
                        .with(user(TEST_THERAPIST).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(homeProgramId))
                .andExpect(jsonPath("$[0].title").value("Evening Dressing Routine"))
                .andExpect(jsonPath("$[0].childName").value("Rohan Verma"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @DisplayName("Reviewing a submitted practice session with therapistFeedback persists feedback and clears pending review count")
    void reviewingPracticeSessionClearsPendingCount() throws Exception {
        Map<String, Object> body = Map.of(
                "therapistFeedback", "Great consistency today!",
                "status", "REVIEWED",
                "flaggedForFollowup", false
        );

        mockMvc.perform(post("/api/caregiver-practice/reviews/" + practiceId)
                        .with(user(TEST_THERAPIST).roles("THERAPIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"))
                .andExpect(jsonPath("$.therapistFeedback").value("Great consistency today!"));

        mockMvc.perform(get("/api/therapist/dashboard-summary")
                        .with(user(TEST_THERAPIST).roles("THERAPIST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingReviewsCount").value(0))
                .andExpect(jsonPath("$.pendingReviews", hasSize(0)));
    }
}
