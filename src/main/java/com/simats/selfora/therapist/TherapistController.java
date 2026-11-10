package com.simats.selfora.therapist;

import com.simats.selfora.dto.HomeProgramDtos;
import com.simats.selfora.dto.SessionDtos;
import com.simats.selfora.dto.TherapistDtos.*;
import com.simats.selfora.homeprogram.HomeProgramService;
import com.simats.selfora.session.TherapySessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('THERAPIST', 'SUPER_ADMIN', 'ADMIN') || hasAnyAuthority('ROLE_THERAPIST', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
public class TherapistController {

    @Autowired
    private TherapistService therapistService;

    @Autowired
    private TherapySessionService therapySessionService;

    @Autowired
    private HomeProgramService homeProgramService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<TherapistDashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(therapistService.getDashboardSummary());
    }

    @GetMapping("/sessions/today")
    public ResponseEntity<List<SessionDtos.SessionResponse>> getTodaySessions() {
        return ResponseEntity.ok(therapySessionService.getTodaySessionsForCurrentUser());
    }

    @GetMapping("/home-programs")
    public ResponseEntity<List<HomeProgramDtos.HomeProgramResponse>> getAssignedHomePrograms() {
        return ResponseEntity.ok(homeProgramService.getAssignedHomeProgramsForCurrentUser());
    }

    @PostMapping("/create-child-with-caregiver")
    public ResponseEntity<CredentialsSuccessResponse> createChildWithCaregiver(@RequestBody CreateChildWithCaregiverRequest request) {
        return ResponseEntity.ok(therapistService.createChildWithCaregiver(request));
    }

    @GetMapping("/children")
    public ResponseEntity<List<ChildSummaryItem>> getChildren(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(therapistService.getChildren(filter, search));
    }

    @GetMapping("/caregivers")
    public ResponseEntity<List<TherapistCaregiverSummaryItem>> getCaregivers() {
        return ResponseEntity.ok(therapistService.getCaregivers());
    }

    @PostMapping("/children/{childId}/link-caregiver")
    public ResponseEntity<Void> linkCaregiverToChild(
            @PathVariable Long childId,
            @RequestParam Long caregiverId,
            @RequestParam(required = false, defaultValue = "Parent") String relationship
    ) {
        therapistService.linkCaregiverToChild(childId, caregiverId, relationship);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/caregivers/{caregiverId}/reset-password")
    public ResponseEntity<ResetPasswordResultResponse> resetCaregiverPassword(@PathVariable Long caregiverId) {
        return ResponseEntity.ok(therapistService.resetCaregiverPassword(caregiverId));
    }

    @PostMapping("/caregivers/{caregiverId}/toggle-status")
    public ResponseEntity<Void> toggleCaregiverStatus(
            @PathVariable Long caregiverId,
            @RequestParam boolean active
    ) {
        therapistService.toggleCaregiverStatus(caregiverId, active);
        return ResponseEntity.ok().build();
    }
}
