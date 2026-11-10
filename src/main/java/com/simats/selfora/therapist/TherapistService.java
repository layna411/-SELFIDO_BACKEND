package com.simats.selfora.therapist;

import com.simats.selfora.dto.TherapistDtos.*;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.Role;
import com.simats.selfora.entity.User;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.repository.ChildRepository;
import com.simats.selfora.repository.RoleRepository;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TherapistService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TherapistService.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateUniqueTemporaryPassword() {
        StringBuilder sb = new StringBuilder("SELF-");
        for (int i = 0; i < 4; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        sb.append("-");
        for (int i = 0; i < 4; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public TherapistDashboardSummaryResponse getDashboardSummary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));

        boolean isAdmin = therapist.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_SUPER_ADMIN"));

        List<Child> children = isAdmin ? childRepository.findAll() : childRepository.findByTherapistId(therapist.getId());
        int activeChildrenCount = children.size();

        int linkedCaregiversCount = 0;
        int pendingAssessmentsCount = 0;
        int activeHomeProgramsCount = 0;
        int todaySessionsCount = 0;
        int completedSessionsCount = 0;
        int pendingReviewsCount = 0;

        LocalDate today = LocalDate.now();
        Timestamp todayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp todayEnd = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        if (isAdmin) {
            Integer cgCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT cc.caregiver_id) FROM caregiver_children cc JOIN children c ON cc.child_id = c.id WHERE (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class
            );
            linkedCaregiversCount = cgCount != null ? cgCount : 0;

            Integer pendingAss = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id) FROM children c LEFT JOIN assessments a ON c.id = a.child_id WHERE a.id IS NULL AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class
            );
            pendingAssessmentsCount = pendingAss != null ? pendingAss : 0;

            Integer activeHp = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT hp.id) FROM home_programs hp JOIN children c ON hp.child_id = c.id " +
                    "WHERE hp.is_active = true AND (hp.status IS NULL OR UPPER(hp.status) NOT IN ('COMPLETED', 'CANCELLED', 'PAUSED')) " +
                    "AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class
            );
            activeHomeProgramsCount = activeHp != null ? activeHp : 0;

            Integer todaySess = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT ts.id) FROM therapy_sessions ts JOIN children c ON ts.child_id = c.id " +
                    "WHERE (c.is_active = true OR c.is_active IS NULL) AND ts.start_time >= ? AND ts.start_time < ?",
                    Integer.class,
                    todayStart, todayEnd
            );
            todaySessionsCount = todaySess != null ? todaySess : 0;

            Integer compSess = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT ts.id) FROM therapy_sessions ts JOIN children c ON ts.child_id = c.id " +
                    "WHERE (c.is_active = true OR c.is_active IS NULL) AND ts.end_time IS NOT NULL",
                    Integer.class
            );
            completedSessionsCount = compSess != null ? compSess : 0;

            Integer pRev = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT cps.id) FROM caregiver_practice_sessions cps JOIN children c ON cps.child_id = c.id " +
                    "WHERE cps.status = 'SUBMITTED' AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class
            );
            pendingReviewsCount = pRev != null ? pRev : 0;
        } else {
            Integer caregiverCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT cc.caregiver_id) FROM caregiver_children cc " +
                    "JOIN children c ON cc.child_id = c.id " +
                    "JOIN therapist_children tc ON c.id = tc.child_id " +
                    "WHERE tc.therapist_id = ? AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class,
                    therapist.getId()
            );
            linkedCaregiversCount = caregiverCount != null ? caregiverCount : 0;

            Integer pendingAss = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id) FROM children c " +
                    "JOIN therapist_children tc ON c.id = tc.child_id " +
                    "LEFT JOIN assessments a ON c.id = a.child_id " +
                    "WHERE tc.therapist_id = ? AND a.id IS NULL AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class,
                    therapist.getId()
            );
            pendingAssessmentsCount = pendingAss != null ? pendingAss : 0;

            Integer activeHp = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT hp.id) FROM home_programs hp " +
                    "JOIN children c ON hp.child_id = c.id " +
                    "JOIN therapist_children tc ON c.id = tc.child_id " +
                    "WHERE tc.therapist_id = ? AND hp.is_active = true " +
                    "AND (hp.status IS NULL OR UPPER(hp.status) NOT IN ('COMPLETED', 'CANCELLED', 'PAUSED')) " +
                    "AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class,
                    therapist.getId()
            );
            activeHomeProgramsCount = activeHp != null ? activeHp : 0;

            Integer todaySess = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT ts.id) FROM therapy_sessions ts " +
                    "JOIN children c ON ts.child_id = c.id " +
                    "JOIN therapist_children tc ON c.id = tc.child_id " +
                    "WHERE tc.therapist_id = ? AND (c.is_active = true OR c.is_active IS NULL) " +
                    "AND ts.start_time >= ? AND ts.start_time < ?",
                    Integer.class,
                    therapist.getId(), todayStart, todayEnd
            );
            todaySessionsCount = todaySess != null ? todaySess : 0;

            Integer compSess = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT ts.id) FROM therapy_sessions ts " +
                    "JOIN children c ON ts.child_id = c.id " +
                    "JOIN therapist_children tc ON c.id = tc.child_id " +
                    "WHERE tc.therapist_id = ? AND (c.is_active = true OR c.is_active IS NULL) AND ts.end_time IS NOT NULL",
                    Integer.class,
                    therapist.getId()
            );
            completedSessionsCount = compSess != null ? compSess : 0;

            Integer pRev = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT cps.id) FROM caregiver_practice_sessions cps " +
                    "JOIN children c ON cps.child_id = c.id " +
                    "JOIN therapist_children tc ON c.id = tc.child_id " +
                    "WHERE tc.therapist_id = ? AND cps.status = 'SUBMITTED' AND (c.is_active = true OR c.is_active IS NULL)",
                    Integer.class,
                    therapist.getId()
            );
            pendingReviewsCount = pRev != null ? pRev : 0;
        }

        List<ScheduledSessionItem> todaySchedule = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = isAdmin
                    ? jdbcTemplate.queryForList(
                            "SELECT DISTINCT ts.id as session_id, ts.child_id, " +
                            "TRIM(CONCAT(c.first_name, ' ', COALESCE(c.last_name, ''))) as child_name, " +
                            "ts.start_time, COALESCE(a.title, 'ADL Session') as adl_activity, " +
                            "CASE WHEN ts.end_time IS NOT NULL THEN 'COMPLETED' ELSE 'IN_PROGRESS' END as status " +
                            "FROM therapy_sessions ts " +
                            "JOIN children c ON ts.child_id = c.id " +
                            "LEFT JOIN activities a ON ts.activity_id = a.id " +
                            "WHERE (c.is_active = true OR c.is_active IS NULL) " +
                            "AND ts.start_time >= ? AND ts.start_time < ? " +
                            "ORDER BY ts.start_time DESC",
                            todayStart, todayEnd
                    )
                    : jdbcTemplate.queryForList(
                            "SELECT DISTINCT ts.id as session_id, ts.child_id, " +
                            "TRIM(CONCAT(c.first_name, ' ', COALESCE(c.last_name, ''))) as child_name, " +
                            "ts.start_time, COALESCE(a.title, 'ADL Session') as adl_activity, " +
                            "CASE WHEN ts.end_time IS NOT NULL THEN 'COMPLETED' ELSE 'IN_PROGRESS' END as status " +
                            "FROM therapy_sessions ts " +
                            "JOIN children c ON ts.child_id = c.id " +
                            "JOIN therapist_children tc ON c.id = tc.child_id " +
                            "LEFT JOIN activities a ON ts.activity_id = a.id " +
                            "WHERE tc.therapist_id = ? AND (c.is_active = true OR c.is_active IS NULL) " +
                            "AND ts.start_time >= ? AND ts.start_time < ? " +
                            "ORDER BY ts.start_time DESC",
                            therapist.getId(), todayStart, todayEnd
                    );
            for (Map<String, Object> r : rows) {
                String timeFormatted = formatTimeCell(r.get("start_time"));
                todaySchedule.add(new ScheduledSessionItem(
                        ((Number) r.get("session_id")).longValue(),
                        ((Number) r.get("child_id")).longValue(),
                        (String) r.get("child_name"),
                        timeFormatted,
                        (String) r.get("adl_activity"),
                        (String) r.get("status")
                ));
            }
        } catch (Exception ex) {
            log.error("Failed to fetch todaySchedule for dashboard", ex);
        }

        List<PendingReviewItem> pendingReviews = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = isAdmin
                    ? jdbcTemplate.queryForList(
                            "SELECT DISTINCT cps.id as practice_id, cps.child_id, " +
                            "TRIM(CONCAT(c.first_name, ' ', COALESCE(c.last_name, ''))) as child_name, " +
                            "COALESCE(u.full_name, 'Caregiver') as caregiver_name, " +
                            "COALESCE(hp.title, a.title, 'Home Program') as activity_name, " +
                            "DATE_FORMAT(COALESCE(cps.submitted_at, cps.created_at), '%Y-%m-%d') as submitted_date, " +
                            "COALESCE(NULLIF(TRIM(cps.caregiver_notes), ''), 'Logged home practice') as notes, " +
                            "COALESCE(cps.submitted_at, cps.created_at) as sort_ts " +
                            "FROM caregiver_practice_sessions cps " +
                            "JOIN children c ON cps.child_id = c.id " +
                            "LEFT JOIN users u ON cps.caregiver_id = u.id " +
                            "LEFT JOIN home_programs hp ON cps.home_program_id = hp.id " +
                            "LEFT JOIN activities a ON cps.activity_id = a.id " +
                            "WHERE cps.status = 'SUBMITTED' AND (c.is_active = true OR c.is_active IS NULL) " +
                            "ORDER BY sort_ts DESC"
                    )
                    : jdbcTemplate.queryForList(
                            "SELECT DISTINCT cps.id as practice_id, cps.child_id, " +
                            "TRIM(CONCAT(c.first_name, ' ', COALESCE(c.last_name, ''))) as child_name, " +
                            "COALESCE(u.full_name, 'Caregiver') as caregiver_name, " +
                            "COALESCE(hp.title, a.title, 'Home Program') as activity_name, " +
                            "DATE_FORMAT(COALESCE(cps.submitted_at, cps.created_at), '%Y-%m-%d') as submitted_date, " +
                            "COALESCE(NULLIF(TRIM(cps.caregiver_notes), ''), 'Logged home practice') as notes, " +
                            "COALESCE(cps.submitted_at, cps.created_at) as sort_ts " +
                            "FROM caregiver_practice_sessions cps " +
                            "JOIN children c ON cps.child_id = c.id " +
                            "JOIN therapist_children tc ON c.id = tc.child_id " +
                            "LEFT JOIN users u ON cps.caregiver_id = u.id " +
                            "LEFT JOIN home_programs hp ON cps.home_program_id = hp.id " +
                            "LEFT JOIN activities a ON cps.activity_id = a.id " +
                            "WHERE tc.therapist_id = ? AND cps.status = 'SUBMITTED' AND (c.is_active = true OR c.is_active IS NULL) " +
                            "ORDER BY sort_ts DESC",
                            therapist.getId()
                    );
            for (Map<String, Object> r : rows) {
                pendingReviews.add(new PendingReviewItem(
                        ((Number) r.get("practice_id")).longValue(),
                        ((Number) r.get("child_id")).longValue(),
                        (String) r.get("child_name"),
                        (String) r.get("caregiver_name"),
                        (String) r.get("activity_name"),
                        (String) r.get("submitted_date"),
                        (String) r.get("notes")
                ));
            }
        } catch (Exception ex) {
            log.error("Failed to fetch pendingReviews for dashboard", ex);
        }


        return new TherapistDashboardSummaryResponse(
                therapist.getFullName(),
                "Senior Occupational Specialist",
                "ic_therapist_default",
                activeChildrenCount,
                linkedCaregiversCount,
                pendingAssessmentsCount,
                activeHomeProgramsCount,
                todaySessionsCount,
                completedSessionsCount,
                pendingReviewsCount,
                todaySchedule,
                pendingReviews
        );
    }

    private String formatTimeCell(Object tsObj) {
        if (tsObj instanceof Timestamp ts) {
            return ts.toLocalDateTime().format(TIME_FMT);
        } else if (tsObj instanceof LocalDateTime ldt) {
            return ldt.format(TIME_FMT);
        }
        return "Today";
    }

    @Transactional
    public CredentialsSuccessResponse createChildWithCaregiver(CreateChildWithCaregiverRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));

        // Step 1: Create or find Caregiver User
        String caregiverEmail = req.getEmail().trim().toLowerCase();
        Optional<User> existingCaregiver = userRepository.findByEmail(caregiverEmail);

        User caregiver;
        String tempPassword;

        if (existingCaregiver.isPresent()) {
            caregiver = existingCaregiver.get();
            tempPassword = "[Existing Caregiver Account Linked]";
        } else {
            tempPassword = generateUniqueTemporaryPassword();
            String hashedPass = passwordEncoder.encode(tempPassword);

            Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("ROLE_CAREGIVER")
                            .description("Caregiver Role")
                            .build()));

            Set<Role> roles = new HashSet<>();
            roles.add(caregiverRole);

            caregiver = User.builder()
                    .username(caregiverEmail)
                    .fullName(req.getCaregiverName())
                    .email(caregiverEmail)
                    .phone(req.getPhone())
                    .passwordHash(hashedPass)
                    .isActive(true)
                    .mustChangePassword(true)
                    .roles(roles)
                    .build();

            caregiver = userRepository.save(caregiver);
        }

        // Step 2: Create Child
        Child child = Child.builder()
                .firstName(req.getChildFirstName())
                .lastName(req.getChildLastName() != null ? req.getChildLastName() : "")
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender() != null ? req.getGender() : "BOY")
                .avatarUrl(req.getProfilePhotoUrl() != null && !req.getProfilePhotoUrl().isBlank() ? req.getProfilePhotoUrl() : "ic_avatar_default")
                .diagnosisNotes(req.getDiagnosisNotes())
                .isActive(true)
                .build();

        child = childRepository.save(child);

        // Step 3: Link Therapist to Child
        jdbcTemplate.update(
                "INSERT INTO therapist_children (therapist_id, child_id) VALUES (?, ?)",
                therapist.getId(), child.getId()
        );

        // Step 4: Link Caregiver to Child
        jdbcTemplate.update(
                "INSERT INTO caregiver_children (caregiver_id, child_id, relationship) VALUES (?, ?, ?)",
                caregiver.getId(), child.getId(), req.getRelationship() != null ? req.getRelationship() : "Mother"
        );

        return new CredentialsSuccessResponse(
                child.getId(),
                child.getFirstName() + (child.getLastName().isBlank() ? "" : " " + child.getLastName()),
                caregiver.getId(),
                caregiver.getFullName(),
                req.getRelationship() != null ? req.getRelationship() : "Mother",
                caregiver.getEmail(),
                tempPassword,
                caregiver.getMustChangePassword()
        );
    }

    public List<ChildSummaryItem> getChildren(String filter, String search) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));

        boolean isAdmin = therapist.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_SUPER_ADMIN"));

        List<Child> children = isAdmin ? childRepository.findAll() : childRepository.findByTherapistId(therapist.getId());

        List<ChildSummaryItem> result = new ArrayList<>();
        for (Child c : children) {
            String fullName = c.getFirstName() + (c.getLastName() == null || c.getLastName().isBlank() ? "" : " " + c.getLastName());

            if (search != null && !search.isBlank()) {
                String searchLower = search.toLowerCase();
                boolean matchesName = fullName.toLowerCase().contains(searchLower);
                boolean matchesId = c.getId().toString().contains(searchLower);
                if (!matchesName && !matchesId) {
                    continue;
                }
            }

            int age = c.getDateOfBirth() != null ? Period.between(c.getDateOfBirth(), LocalDate.now()).getYears() : 6;

            // Fetch linked caregiver
            List<Map<String, Object>> cgRows = jdbcTemplate.queryForList(
                    "SELECT u.full_name, cc.relationship FROM users u JOIN caregiver_children cc ON u.id = cc.caregiver_id WHERE cc.child_id = ?",
                    c.getId()
            );
            String cgName = !cgRows.isEmpty() ? (String) cgRows.get(0).get("full_name") : "Unassigned";
            String cgRel = !cgRows.isEmpty() ? (String) cgRows.get(0).get("relationship") : "Parent";

            boolean hasAssessment = !jdbcTemplate.queryForList("SELECT id FROM assessments WHERE child_id = ?", c.getId()).isEmpty();
            boolean hasHp = !jdbcTemplate.queryForList("SELECT id FROM home_programs WHERE child_id = ? AND is_active = true", c.getId()).isEmpty();

            String status = !hasAssessment ? "PENDING_ASSESSMENT" : (hasHp ? "ACTIVE" : "COMPLETED");

            if ("ASSESSMENT_PENDING".equalsIgnoreCase(filter) || "PENDING_ASSESSMENT".equalsIgnoreCase(filter)) {
                if (hasAssessment) continue;
            }
            if ("HOME_PROGRAM_ACTIVE".equalsIgnoreCase(filter) || "ACTIVE".equalsIgnoreCase(filter)) {
                if (!hasHp) continue;
            }
            if ("COMPLETED".equalsIgnoreCase(filter)) {
                if (!"COMPLETED".equalsIgnoreCase(status)) continue;
            }

            int dressingPct = calculateCategoryProgress(c.getId(), "Dressing");
            int eatingPct = calculateCategoryProgress(c.getId(), "Eating");
            int shoesPct = calculateCategoryProgress(c.getId(), "Shoes");
            int overallPct = (dressingPct + eatingPct + shoesPct) / 3;

            // Fetch last session date
            String lastSessDate = "2026-10-02";
            try {
                List<String> dates = jdbcTemplate.queryForList(
                        "SELECT DATE_FORMAT(start_time, '%Y-%m-%d') FROM therapy_sessions WHERE child_id = ? ORDER BY start_time DESC LIMIT 1",
                        String.class, c.getId()
                );
                if (!dates.isEmpty() && dates.get(0) != null) {
                    lastSessDate = dates.get(0);
                }
            } catch (Exception ignored) {}

            result.add(new ChildSummaryItem(
                    c.getId(),
                    fullName,
                    age > 0 ? age : 6,
                    c.getGender() != null ? c.getGender() : "BOY",
                    c.getAvatarUrl() != null ? c.getAvatarUrl() : "ic_avatar_default",
                    cgName,
                    cgRel,
                    lastSessDate,
                    status,
                    3,
                    dressingPct,
                    eatingPct,
                    shoesPct,
                    overallPct,
                    !hasAssessment,
                    hasHp
            ));
        }
        return result;
    }

    private int calculateCategoryProgress(Long childId, String categoryName) {
        try {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(pr.id) FROM performance_records pr " +
                    "JOIN therapy_sessions ts ON pr.session_id = ts.id " +
                    "LEFT JOIN activities a ON ts.activity_id = a.id " +
                    "WHERE ts.child_id = ? AND (a.category = ? OR a.name LIKE ?)",
                    Integer.class,
                    childId, categoryName, "%" + categoryName + "%"
            );
            if (total == null || total == 0) return 75;
            Integer independent = jdbcTemplate.queryForObject(
                    "SELECT COUNT(pr.id) FROM performance_records pr " +
                    "JOIN therapy_sessions ts ON pr.session_id = ts.id " +
                    "JOIN prompt_levels pl ON pr.prompt_level_id = pl.id " +
                    "LEFT JOIN activities a ON ts.activity_id = a.id " +
                    "WHERE ts.child_id = ? AND (a.category = ? OR a.name LIKE ?) AND (pl.code = 'L0' OR pl.name LIKE '%Independent%')",
                    Integer.class,
                    childId, categoryName, "%" + categoryName + "%"
            );
            int ind = independent != null ? independent : 0;
            return (int) Math.round(((double) ind / total) * 100);
        } catch (Exception e) {
            return 75;
        }
    }

    public List<TherapistCaregiverSummaryItem> getCaregivers() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DISTINCT u.id as caregiver_id, u.full_name, u.email, u.phone, u.is_active, " +
                "cc.relationship, c.id as child_id, CONCAT(c.first_name, ' ', COALESCE(c.last_name, '')) as child_name " +
                "FROM users u " +
                "JOIN caregiver_children cc ON u.id = cc.caregiver_id " +
                "JOIN children c ON cc.child_id = c.id " +
                "JOIN therapist_children tc ON c.id = tc.child_id " +
                "WHERE tc.therapist_id = ?",
                therapist.getId()
        );

        List<TherapistCaregiverSummaryItem> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Long caregiverId = ((Number) r.get("caregiver_id")).longValue();
            Long childId = ((Number) r.get("child_id")).longValue();

            Integer activeHp = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM home_programs WHERE child_id = ? AND is_active = true",
                    Integer.class, childId
            );
            Integer pendingRev = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM caregiver_practice_sessions WHERE caregiver_id = ? AND child_id = ? AND status = 'SUBMITTED'",
                    Integer.class, caregiverId, childId
            );
            Integer unreadMessages = 0;
            try {
                unreadMessages = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM messages WHERE child_id = ? AND receiver_id = ? AND is_read = false",
                        Integer.class, childId, therapist.getId()
                );
            } catch (Exception ignored) {}

            String lastMsg = null;
            String lastMsgAtIso = null;
            Long lastSenderId = null;
            try {
                List<Map<String, Object>> lastMsgs = jdbcTemplate.queryForList(
                        "SELECT message_text, sent_at, sender_id FROM messages WHERE child_id = ? ORDER BY sent_at DESC, id DESC LIMIT 1",
                        childId
                );
                if (!lastMsgs.isEmpty()) {
                    Map<String, Object> lm = lastMsgs.get(0);
                    lastMsg = (String) lm.get("message_text");
                    Object sentObj = lm.get("sent_at");
                    if (sentObj instanceof java.sql.Timestamp) {
                        lastMsgAtIso = ((java.sql.Timestamp) sentObj).toLocalDateTime().toString();
                    } else if (sentObj instanceof java.time.LocalDateTime) {
                        lastMsgAtIso = ((java.time.LocalDateTime) sentObj).toString();
                    } else if (sentObj != null) {
                        lastMsgAtIso = sentObj.toString();
                    }
                    if (lm.get("sender_id") instanceof Number) {
                        lastSenderId = ((Number) lm.get("sender_id")).longValue();
                    }
                }
            } catch (Exception ignored) {}

            Boolean isActive = true;
            Object activeObj = r.get("is_active");
            if (activeObj instanceof Boolean) {
                isActive = (Boolean) activeObj;
            } else if (activeObj instanceof Number) {
                isActive = ((Number) activeObj).intValue() == 1;
            }

            int pendingReviews = pendingRev != null ? pendingRev : 0;
            int unreadMsgs = unreadMessages != null ? unreadMessages : 0;

            TherapistCaregiverSummaryItem item = new TherapistCaregiverSummaryItem(
                    caregiverId,
                    (String) r.get("full_name"),
                    (String) r.get("email"),
                    r.get("phone") != null ? (String) r.get("phone") : "+91 9876543210",
                    isActive,
                    (String) r.get("relationship"),
                    childId,
                    (String) r.get("child_name"),
                    activeHp != null ? activeHp : 0,
                    pendingReviews
            );
            item.setLastMessage(lastMsg);
            item.setLastMessageAt(lastMsgAtIso);
            item.setLastSenderId(lastSenderId);
            item.setUnreadMessagesCount(unreadMsgs);
            result.add(item);
        }

        // Order by most recent message first; contacts without messages stay below alphabetically
        result.sort((a, b) -> {
            String ta = a.getLastMessageAt();
            String tb = b.getLastMessageAt();
            if (ta == null && tb == null) {
                String na = a.getFullName() != null ? a.getFullName() : "";
                String nb = b.getFullName() != null ? b.getFullName() : "";
                return na.compareToIgnoreCase(nb);
            }
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return result;
    }

    @Transactional
    public void linkCaregiverToChild(Long childId, Long caregiverId, String relationship) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User therapist = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));

        Integer assignedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM therapist_children WHERE child_id = ? AND therapist_id = ?",
                Integer.class, childId, therapist.getId()
        );
        if (assignedCount == null || assignedCount == 0) {
            throw new org.springframework.security.access.AccessDeniedException("Therapist is not authorized to manage relationships for child ID: " + childId);
        }

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver not found with ID: " + caregiverId));

        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM caregiver_children WHERE child_id = ? AND caregiver_id = ?",
                Integer.class, childId, caregiverId
        );
        if (existingCount != null && existingCount > 0) {
            jdbcTemplate.update(
                    "UPDATE caregiver_children SET relationship = ? WHERE child_id = ? AND caregiver_id = ?",
                    relationship != null ? relationship : "Parent", childId, caregiverId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO caregiver_children (caregiver_id, child_id, relationship) VALUES (?, ?, ?)",
                    caregiverId, childId, relationship != null ? relationship : "Parent"
            );
        }
    }

    @Transactional
    public ResetPasswordResultResponse resetCaregiverPassword(Long caregiverId) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver not found with ID: " + caregiverId));

        String newTempPassword = generateUniqueTemporaryPassword();
        caregiver.setPasswordHash(passwordEncoder.encode(newTempPassword));
        caregiver.setMustChangePassword(true);
        userRepository.save(caregiver);

        return new ResetPasswordResultResponse(
                caregiver.getId(),
                caregiver.getEmail(),
                newTempPassword,
                "Temporary password reset successfully. Share this temporary password with the caregiver."
        );
    }

    @Transactional
    public void toggleCaregiverStatus(Long caregiverId, boolean active) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver not found with ID: " + caregiverId));
        caregiver.setIsActive(active);
        userRepository.save(caregiver);
    }
}
