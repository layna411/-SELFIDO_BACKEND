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

    public List<User> getAllTherapists() {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST").orElse(null);
        if (therapistRole == null) return Collections.emptyList();
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(therapistRole))
                .collect(Collectors.toList());
    }

    public User createTherapist(AdminDtos.TherapistCreateRequest req, Long actorId) {
        Role therapistRole = roleRepository.findByName("ROLE_THERAPIST")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_THERAPIST", "Therapist Role")));

        String encodedPass = passwordEncoder.encode(req.getPassword() != null ? req.getPassword() : "password123");
        User therapist = User.builder()
                .username(req.getUsername())
                .passwordHash(encodedPass)
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .isActive(true)
                .mustChangePassword(true)
                .roles(Collections.singleton(therapistRole))
                .build();

        User saved = userRepository.save(therapist);
        logAudit(actorId, "ROLE_SUPER_ADMIN", "CREATE_THERAPIST", "Therapist ID: " + saved.getId(), "Created therapist: " + saved.getFullName());
        return saved;
    }

    @Transactional
    public User updateTherapistStatus(Long therapistId, boolean isActive, Long actorId) {
        User user = userRepository.findById(therapistId)
                .orElseThrow(() -> new RuntimeException("Therapist not found"));
        user.setIsActive(isActive);
        User saved = userRepository.save(user);

        logAudit(actorId, "ROLE_SUPER_ADMIN", isActive ? "ACTIVATE_THERAPIST" : "DEACTIVATE_THERAPIST", "Therapist ID: " + therapistId, "Updated active status to: " + isActive);
        return saved;
    }

    public List<User> getAllCaregivers() {
        Role caregiverRole = roleRepository.findByName("ROLE_CAREGIVER").orElse(null);
        if (caregiverRole == null) return Collections.emptyList();
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(caregiverRole))
                .collect(Collectors.toList());
    }

    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    @Transactional
    public User updateCaregiverStatus(Long caregiverId, boolean isActive, Long actorId) {
        User user = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));
        user.setIsActive(isActive);
        User saved = userRepository.save(user);

        logAudit(actorId, "ROLE_SUPER_ADMIN", isActive ? "ACTIVATE_CAREGIVER" : "DEACTIVATE_CAREGIVER", "Caregiver ID: " + caregiverId, "Updated active status to: " + isActive);
        return saved;
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
