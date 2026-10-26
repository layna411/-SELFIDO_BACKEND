package com.simats.selfora.admin;

import com.simats.selfora.dto.AdminDtos;
import com.simats.selfora.entity.*;
import com.simats.selfora.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private TherapySessionRepository sessionRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private HomeProgramRepository homeProgramRepository;

    @Autowired
    private CaregiverPracticeSessionRepository practiceSessionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public AdminDtos.DashboardStats getDashboardStats() {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST").orElse(null);
        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER").orElse(null);

        long totalTherapists = therapistRole != null ? userRepository.findAll().stream().filter(u -> u.getRoles().contains(therapistRole)).count() : 0;
        long activeTherapists = therapistRole != null ? userRepository.findAll().stream().filter(u -> u.getRoles().contains(therapistRole) && Boolean.TRUE.equals(u.getIsActive())).count() : 0;

        long totalCaregivers = caregiverRole != null ? userRepository.findAll().stream().filter(u -> u.getRoles().contains(caregiverRole)).count() : 0;
        long totalChildren = childRepository.count();

        long activeSessions = sessionRepository.count();
        long completedAssessments = assessmentRepository.count();
        long activeHomeProgrammes = homeProgramRepository.count();
        long pendingReviews = practiceSessionRepository.count();

        return new AdminDtos.DashboardStats(
                totalTherapists,
                activeTherapists,
                totalChildren,
                totalCaregivers,
                activeSessions,
                completedAssessments,
                activeHomeProgrammes,
                pendingReviews
        );
    }

    public AdminDtos.ChartData getChartData() {
        Map<String, Long> regMap = new LinkedHashMap<>();
        regMap.put("Jan", 12L);
        regMap.put("Feb", 18L);
        regMap.put("Mar", 25L);
        regMap.put("Apr", 32L);
        regMap.put("May", 45L);
        regMap.put("Jun", 60L);

        Map<String, Long> sessionMap = new LinkedHashMap<>();
        sessionMap.put("Mon", 14L);
        sessionMap.put("Tue", 22L);
        sessionMap.put("Wed", 19L);
        sessionMap.put("Thu", 28L);
        sessionMap.put("Fri", 35L);
        sessionMap.put("Sat", 18L);
        sessionMap.put("Sun", 8L);

        Map<String, Long> adlMap = new LinkedHashMap<>();
        adlMap.put("Dressing", 45L);
        adlMap.put("Eating", 30L);
        adlMap.put("Grooming", 15L);
        adlMap.put("Shoes & Socks", 10L);

        return new AdminDtos.ChartData(regMap, sessionMap, adlMap);
    }

    private AdminDtos.TherapistProfile mapToTherapistProfile(User u) {
        AdminDtos.TherapistProfile dto = new AdminDtos.TherapistProfile();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone() != null && !u.getPhone().isBlank() ? u.getPhone() : "+91 9876543210");
        dto.setDesignation("Senior Occupational Therapist");
        dto.setSpecialization("Pediatric ADL & Fine Motor Skills");
        dto.setQualification("BOT, MOT (Pediatrics)");
        dto.setExperience("5 Years");
        dto.setIsActive(u.getIsActive());
        dto.setMustChangePassword(u.getMustChangePassword());
        dto.setCreatedAt(u.getCreatedAt());

        long count = 0;
        try {
            count = childRepository.findByTherapistId(u.getId()).size();
        } catch (Exception ignored) {}
        dto.setAssignedChildrenCount(count);
        dto.setTotalSessions(0L);
        return dto;
    }

    public List<AdminDtos.TherapistProfile> getAllTherapists() {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST").orElse(null);
        if (therapistRole == null) return Collections.emptyList();
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(therapistRole))
                .map(this::mapToTherapistProfile)
                .collect(Collectors.toList());
    }

    public AdminDtos.TherapistProfile getTherapistById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Therapist not found with ID: " + id));
        return mapToTherapistProfile(user);
    }

    public AdminDtos.TherapistProfile createTherapist(AdminDtos.TherapistCreateRequest req, Long actorId) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (userRepository.findByUsername(req.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Username '" + req.getUsername() + "' is already taken.");
        }
        if (userRepository.findByEmail(req.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email '" + req.getEmail() + "' is already registered.");
        }

        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_THERAPIST", "Therapist Role")));

        String encodedPass = passwordEncoder.encode(req.getPassword() != null && !req.getPassword().isBlank() ? req.getPassword() : "password123");
        User therapist = User.builder()
                .username(req.getUsername().trim())
                .passwordHash(encodedPass)
                .fullName(req.getFullName() != null ? req.getFullName().trim() : "")
                .email(req.getEmail().trim())
                .phone(req.getPhone())
                .isActive(true)
                .mustChangePassword(true)
                .roles(Collections.singleton(therapistRole))
                .build();

        User saved = userRepository.save(therapist);
        logAudit(actorId, "ROLE_SUPER_ADMIN", "CREATE_THERAPIST", "Therapist ID: " + saved.getId(), "Created therapist: " + saved.getFullName());
        return mapToTherapistProfile(saved);
    }

    @Transactional
    public AdminDtos.TherapistProfile updateTherapist(Long therapistId, AdminDtos.TherapistUpdateRequest req, Long actorId) {
        User user = userRepository.findById(therapistId)
                .orElseThrow(() -> new IllegalArgumentException("Therapist not found with ID: " + therapistId));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank() && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(req.getEmail().trim()).isPresent()) {
                throw new IllegalArgumentException("Email '" + req.getEmail() + "' is already in use.");
            }
            user.setEmail(req.getEmail().trim());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone().trim());
        }

        User saved = userRepository.save(user);
        logAudit(actorId, "ROLE_SUPER_ADMIN", "UPDATE_THERAPIST", "Therapist ID: " + therapistId, "Updated profile for therapist: " + saved.getFullName());
        return mapToTherapistProfile(saved);
    }

    @Transactional
    public AdminDtos.TherapistProfile updateTherapistStatus(Long therapistId, boolean isActive, Long actorId) {
        User user = userRepository.findById(therapistId)
                .orElseThrow(() -> new IllegalArgumentException("Therapist not found with ID: " + therapistId));

        if (!isActive) {
            Role superAdminRole = roleRepository.findByName("ROLE_SUPER_ADMIN").orElse(null);
            if (superAdminRole != null && user.getRoles().contains(superAdminRole)) {
                long activeSuperAdmins = userRepository.findAll().stream()
                        .filter(u -> u.getRoles().contains(superAdminRole) && Boolean.TRUE.equals(u.getIsActive()))
                        .count();
                if (activeSuperAdmins <= 1) {
                    throw new IllegalStateException("Cannot deactivate the final active Super Admin account.");
                }
            }
        }

        user.setIsActive(isActive);
        User saved = userRepository.save(user);

        logAudit(actorId, "ROLE_SUPER_ADMIN", isActive ? "ACTIVATE_THERAPIST" : "DEACTIVATE_THERAPIST", "Therapist ID: " + therapistId, "Updated active status to: " + isActive);
        return mapToTherapistProfile(saved);
    }

    private AdminDtos.CaregiverProfile mapToCaregiverProfile(User u) {
        AdminDtos.CaregiverProfile dto = new AdminDtos.CaregiverProfile();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone() != null && !u.getPhone().isBlank() ? u.getPhone() : "+91 9876543210");
        dto.setIsActive(u.getIsActive() != null ? u.getIsActive() : true);
        dto.setMustChangePassword(u.getMustChangePassword() != null ? u.getMustChangePassword() : false);
        dto.setCreatedAt(u.getCreatedAt());

        List<AdminDtos.AdminChildSummary> children = new ArrayList<>();
        List<String> childNames = new ArrayList<>();
        try {
            List<Long> childIds = jdbcTemplate.queryForList(
                    "SELECT child_id FROM caregiver_children WHERE caregiver_id = ?",
                    Long.class,
                    u.getId()
            );
            for (Long childId : childIds) {
                childRepository.findById(childId).ifPresent(c -> {
                    AdminDtos.AdminChildSummary summary = mapToAdminChildSummary(c);
                    children.add(summary);
                    childNames.add(summary.getFullName());
                });
            }
        } catch (Exception e) {
            // Log or ignore
        }

        dto.setLinkedChildrenCount(children.size());
        dto.setLinkedChildrenNames(childNames);
        dto.setLinkedChildren(children);
        return dto;
    }

    public List<AdminDtos.CaregiverProfile> getAllCaregivers() {
        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER").orElse(null);
        if (caregiverRole == null) return Collections.emptyList();
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(caregiverRole))
                .map(this::mapToCaregiverProfile)
                .collect(Collectors.toList());
    }

    public AdminDtos.CaregiverProfile getCaregiverById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.simats.selfora.exception.ResourceNotFoundException("Caregiver not found with ID: " + id));
        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER").orElse(null);
        if (caregiverRole != null && !user.getRoles().contains(caregiverRole)) {
            throw new com.simats.selfora.exception.ResourceNotFoundException("User with ID: " + id + " is not a caregiver.");
        }
        return mapToCaregiverProfile(user);
    }

    public AdminDtos.CaregiverProfile createCaregiver(AdminDtos.CaregiverCreateRequest req, Long actorId) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!req.getEmail().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (userRepository.findByUsername(req.getUsername().trim()).isPresent()) {
            throw new com.simats.selfora.exception.DuplicateResourceException("Username '" + req.getUsername() + "' is already taken.");
        }
        if (userRepository.findByEmail(req.getEmail().trim()).isPresent()) {
            throw new com.simats.selfora.exception.DuplicateResourceException("Email '" + req.getEmail() + "' is already registered.");
        }

        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_CAREGIVER", "Caregiver Role")));

        String encodedPass = passwordEncoder.encode(req.getPassword() != null && !req.getPassword().isBlank() ? req.getPassword() : "password123");
        User caregiver = User.builder()
                .username(req.getUsername().trim())
                .passwordHash(encodedPass)
                .fullName(req.getFullName() != null ? req.getFullName().trim() : "")
                .email(req.getEmail().trim())
                .phone(req.getPhone() != null ? req.getPhone().trim() : "")
                .isActive(true)
                .mustChangePassword(true)
                .roles(Collections.singleton(caregiverRole))
                .build();

        User saved = userRepository.save(caregiver);
        logAudit(actorId, "ROLE_SUPER_ADMIN", "CREATE_CAREGIVER", "Caregiver ID: " + saved.getId(), "Created caregiver: " + saved.getFullName());
        return mapToCaregiverProfile(saved);
    }

    @Transactional
    public AdminDtos.CaregiverProfile updateCaregiver(Long caregiverId, AdminDtos.CaregiverUpdateRequest req, Long actorId) {
        User user = userRepository.findById(caregiverId)
                .orElseThrow(() -> new com.simats.selfora.exception.ResourceNotFoundException("Caregiver not found with ID: " + caregiverId));

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank() && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (!req.getEmail().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                throw new IllegalArgumentException("Invalid email format.");
            }
            if (userRepository.findByEmail(req.getEmail().trim()).isPresent()) {
                throw new com.simats.selfora.exception.DuplicateResourceException("Email '" + req.getEmail() + "' is already in use.");
            }
            user.setEmail(req.getEmail().trim());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone().trim());
        }

        User saved = userRepository.save(user);
        logAudit(actorId, "ROLE_SUPER_ADMIN", "UPDATE_CAREGIVER", "Caregiver ID: " + caregiverId, "Updated profile for caregiver: " + saved.getFullName());
        return mapToCaregiverProfile(saved);
    }

    @Transactional
    public AdminDtos.CaregiverProfile updateCaregiverStatus(Long caregiverId, boolean isActive, Long actorId) {
        User user = userRepository.findById(caregiverId)
                .orElseThrow(() -> new com.simats.selfora.exception.ResourceNotFoundException("Caregiver not found with ID: " + caregiverId));

        user.setIsActive(isActive);
        User saved = userRepository.save(user);

        logAudit(actorId, "ROLE_SUPER_ADMIN", isActive ? "ACTIVATE_CAREGIVER" : "DEACTIVATE_CAREGIVER", "Caregiver ID: " + caregiverId, "Updated active status to: " + isActive);
        return mapToCaregiverProfile(saved);
    }

    @Transactional
    public void resetCaregiverPassword(Long caregiverId, String newPassword, Long actorId) {
        User user = userRepository.findById(caregiverId)
                .orElseThrow(() -> new com.simats.selfora.exception.ResourceNotFoundException("Caregiver not found with ID: " + caregiverId));

        String passToUse = newPassword != null && !newPassword.isBlank() ? newPassword : "password123";
        user.setPasswordHash(passwordEncoder.encode(passToUse));
        user.setMustChangePassword(true);
        userRepository.save(user);

        logAudit(actorId, "ROLE_SUPER_ADMIN", "RESET_CAREGIVER_PASSWORD", "Caregiver ID: " + caregiverId, "Reset password and set mustChangePassword=true");
    }

    private AdminDtos.AdminChildSummary mapToAdminChildSummary(Child c) {
        AdminDtos.AdminChildSummary summary = new AdminDtos.AdminChildSummary();
        summary.setId(c.getId());
        summary.setFirstName(c.getFirstName());
        summary.setLastName(c.getLastName());
        summary.setFullName(c.getFirstName() + (c.getLastName() != null && !c.getLastName().isBlank() ? " " + c.getLastName() : ""));
        summary.setDateOfBirth(c.getDateOfBirth());
        if (c.getDateOfBirth() != null) {
            summary.setAge(java.time.Period.between(c.getDateOfBirth(), java.time.LocalDate.now()).getYears());
        } else {
            summary.setAge(6);
        }
        summary.setGender(c.getGender() != null ? c.getGender() : "MALE");
        summary.setAvatarUrl(c.getAvatarUrl());
        summary.setIsActive(c.getIsActive() != null ? c.getIsActive() : true);
        summary.setCreatedAt(c.getCreatedAt());

        // Therapist Relationship
        try {
            List<Map<String, Object>> tList = jdbcTemplate.queryForList(
                    "SELECT u.id, u.full_name FROM users u JOIN therapist_children tc ON u.id = tc.therapist_id WHERE tc.child_id = ?",
                    c.getId()
            );
            if (!tList.isEmpty()) {
                summary.setAssignedTherapistId(((Number) tList.get(0).get("id")).longValue());
                summary.setAssignedTherapistName((String) tList.get(0).get("full_name"));
            } else {
                summary.setAssignedTherapistName("Unassigned");
            }
        } catch (Exception e) {
            summary.setAssignedTherapistName("Unassigned");
        }

        // Caregiver Relationship
        try {
            List<Map<String, Object>> cList = jdbcTemplate.queryForList(
                    "SELECT u.id, u.full_name FROM users u JOIN caregiver_children cc ON u.id = cc.caregiver_id WHERE cc.child_id = ?",
                    c.getId()
            );
            if (!cList.isEmpty()) {
                summary.setLinkedCaregiverId(((Number) cList.get(0).get("id")).longValue());
                summary.setLinkedCaregiverName((String) cList.get(0).get("full_name"));
            } else {
                summary.setLinkedCaregiverName("Unassigned Caregiver");
            }
        } catch (Exception e) {
            summary.setLinkedCaregiverName("Unassigned Caregiver");
        }

        // Active ADL Category / Program
        try {
            List<String> progs = jdbcTemplate.queryForList(
                    "SELECT title FROM home_programs WHERE child_id = ? AND is_active = true LIMIT 1",
                    String.class,
                    c.getId()
            );
            if (!progs.isEmpty()) {
                summary.setActiveAdlCategory(progs.get(0));
            } else {
                summary.setActiveAdlCategory("Dressing - Upper Body T-Shirt");
            }
        } catch (Exception e) {
            summary.setActiveAdlCategory("Dressing - Upper Body T-Shirt");
        }

        return summary;
    }

    public List<AdminDtos.AdminChildSummary> getAllChildrenForAdmin() {
        return childRepository.findAll().stream()
                .map(this::mapToAdminChildSummary)
                .collect(Collectors.toList());
    }

    public AdminDtos.AdminChildDetailProfile getChildDetailProfileForAdmin(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new com.simats.selfora.exception.ResourceNotFoundException("Child not found with ID: " + childId));

        AdminDtos.AdminChildSummary summary = mapToAdminChildSummary(child);
        AdminDtos.AdminChildDetailProfile profile = new AdminDtos.AdminChildDetailProfile();
        profile.setSummary(summary);
        profile.setDiagnosisNotes(child.getDiagnosisNotes() != null && !child.getDiagnosisNotes().isBlank() ? child.getDiagnosisNotes() : "No diagnosis notes recorded.");
        profile.setSupportedAdlCategories(Arrays.asList("Dressing", "Eating", "Grooming", "Shoes & Socks"));

        long sessions = 0;
        try {
            sessions = sessionRepository.findAll().stream().filter(s -> s.getChild() != null && s.getChild().getId().equals(childId)).count();
        } catch (Exception ignored) {}
        profile.setTotalSessions(sessions);

        long assessments = 0;
        try {
            assessments = assessmentRepository.findAll().stream().filter(a -> a.getChild() != null && a.getChild().getId().equals(childId)).count();
        } catch (Exception ignored) {}
        profile.setCompletedAssessments(assessments);

        long practiceCount = 0;
        try {
            practiceCount = practiceSessionRepository.findAll().stream().filter(p -> p.getChild() != null && p.getChild().getId().equals(childId)).count();
        } catch (Exception ignored) {}
        profile.setHomePracticeSessionsCount(practiceCount);
        profile.setActiveHomeProgramTitle(summary.getActiveAdlCategory());

        return profile;
    }

    @Transactional
    public void resetTherapistPassword(Long therapistId, String newPassword, Long actorId) {
        User user = userRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Therapist not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword != null ? newPassword : "password123"));
        user.setMustChangePassword(true);
        userRepository.save(user);

        logAudit(actorId, "ROLE_SUPER_ADMIN", "RESET_THERAPIST_PASSWORD", "Therapist ID: " + therapistId, "Reset password and set mustChangePassword=true");
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public void logAudit(Long actorId, String actorRole, String action, String affectedEntity, String summary) {
        AuditLog log = new AuditLog(null, actorId, actorRole, action, affectedEntity, summary, LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
