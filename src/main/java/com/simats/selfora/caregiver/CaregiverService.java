package com.simats.selfora.caregiver;

import com.simats.selfora.dto.CaregiverDtos.*;
import com.simats.selfora.dto.CaregiverPracticeDtos.CaregiverPracticeSessionResponse;
import com.simats.selfora.dto.HomeProgramDtos.HomeProgramResponse;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.User;
import com.simats.selfora.exception.ResourceNotFoundException;
import com.simats.selfora.homeprogram.CaregiverPracticeService;
import com.simats.selfora.homeprogram.HomeProgramService;
import com.simats.selfora.repository.ChildRepository;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
public class CaregiverService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private HomeProgramService homeProgramService;

    @Autowired
    private CaregiverPracticeService caregiverPracticeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public CaregiverDashboardSummaryResponse getDashboardSummary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User caregiver = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver not found"));

        List<Child> children = childRepository.findByCaregiverId(caregiver.getId());
        List<LinkedChildDto> linkedChildren = new ArrayList<>();

        for (Child c : children) {
            int age = c.getDateOfBirth() != null ? Period.between(c.getDateOfBirth(), LocalDate.now()).getYears() : 6;
            String fullName = c.getFirstName() + (c.getLastName() == null || c.getLastName().isBlank() ? "" : " " + c.getLastName());

            String thName = "Dr. Sarah Jenkins";
            try {
                List<String> thNames = jdbcTemplate.queryForList(
                        "SELECT u.full_name FROM users u JOIN therapist_children tc ON u.id = tc.therapist_id WHERE tc.child_id = ? LIMIT 1",
                        String.class, c.getId()
                );
                if (!thNames.isEmpty() && thNames.get(0) != null) {
                    thName = thNames.get(0);
                }
            } catch (Exception ignored) {}

            linkedChildren.add(new LinkedChildDto(
                    c.getId(),
                    fullName,
                    c.getGender() != null ? c.getGender() : "BOY",
                    age > 0 ? age : 6,
                    thName,
                    c.getAvatarUrl() != null ? c.getAvatarUrl() : "ic_avatar_default"
            ));
        }

        List<HomeProgramResponse> todayPrograms = homeProgramService.getHomeProgramsForCaregiver();

        int activeHpCount = 0;
        try {
            Integer hpCnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT hp.id) FROM home_programs hp JOIN caregiver_children cc ON hp.child_id = cc.child_id WHERE cc.caregiver_id = ? AND hp.is_active = true",
                    Integer.class, caregiver.getId()
            );
            activeHpCount = hpCnt != null ? hpCnt : todayPrograms.size();
        } catch (Exception ignored) {
            activeHpCount = todayPrograms.size();
        }

        int totalCompletedPractices = 0;
        List<CaregiverPracticeSessionResponse> recentHistory = new ArrayList<>();
        if (!children.isEmpty()) {
            try {
                recentHistory = caregiverPracticeService.getPracticeHistoryForChild(children.get(0).getId());
                Integer totalComp = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM caregiver_practice_sessions WHERE caregiver_id = ?",
                        Integer.class, caregiver.getId()
                );
                totalCompletedPractices = totalComp != null ? totalComp : recentHistory.size();
            } catch (Exception ignored) {}
        }

        return new CaregiverDashboardSummaryResponse(
                caregiver.getFullName(),
                caregiver.getEmail(),
                linkedChildren,
                totalCompletedPractices,
                activeHpCount,
                todayPrograms,
                recentHistory
        );
    }

    public boolean validateCaregiverChildAccess(Long childId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_SUPER_ADMIN"));
        if (isAdmin) {
            return true;
        }

        long count = childRepository.countByChildIdAndCaregiverId(childId, user.getId());
        if (count == 0) {
            throw new AccessDeniedException("Caregiver " + username + " is not authorized to access child ID: " + childId);
        }
        return true;
    }
}
