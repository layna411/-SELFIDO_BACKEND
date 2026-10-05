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
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TherapistService {

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

        List<Child> children = childRepository.findByTherapistId(therapist.getId());
        int activeChildrenCount = children.size();

        Integer caregiverCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT caregiver_id) FROM caregiver_children cc JOIN children c ON cc.child_id = c.id JOIN therapist_children tc ON c.id = tc.child_id WHERE tc.therapist_id = ?",
                Integer.class,
                therapist.getId()
        );
        int linkedCaregiversCount = caregiverCount != null ? caregiverCount : 0;

        Integer pendingAss = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM children c JOIN therapist_children tc ON c.id = tc.child_id LEFT JOIN assessments a ON c.id = a.child_id WHERE tc.therapist_id = ? AND a.id IS NULL",
                Integer.class,
                therapist.getId()
        );
        int pendingAssessmentsCount = pendingAss != null ? pendingAss : 0;

        Integer activeHp = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM home_programs hp WHERE hp.therapist_id = ? AND hp.is_active = true",
                Integer.class,
                therapist.getId()
        );
        int activeHomeProgramsCount = activeHp != null ? activeHp : 0;

        Integer todaySess = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM therapy_sessions WHERE conducted_by_user_id = ? AND DATE(start_time) = CURRENT_DATE",
                Integer.class,
                therapist.getId()
        );
        int todaySessionsCount = todaySess != null ? todaySess : 0;

        Integer compSess = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM therapy_sessions WHERE conducted_by_user_id = ? AND end_time IS NOT NULL",
                Integer.class,
                therapist.getId()
        );
        int completedSessionsCount = compSess != null ? compSess : 0;

        int pendingReviewsCount = activeHomeProgramsCount;

        List<ScheduledSessionItem> todaySchedule = new ArrayList<>();
        List<PendingReviewItem> pendingReviews = new ArrayList<>();

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

        List<Child> children = childRepository.findByTherapistId(therapist.getId());

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

            result.add(new ChildSummaryItem(
                    c.getId(),
                    fullName,
                    age > 0 ? age : 6,
                    c.getGender() != null ? c.getGender() : "BOY",
                    c.getAvatarUrl() != null ? c.getAvatarUrl() : "ic_avatar_default",
                    cgName,
                    cgRel,
                    "2026-10-02",
                    status,
                    3,
                    75, // dressing %
                    68, // eating %
                    82, // shoes %
                    75, // overall %
                    !hasAssessment,
                    hasHp
            ));
        }
        return result;
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
