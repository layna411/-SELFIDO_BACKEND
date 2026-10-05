package com.simats.selfora.therapist;

import com.simats.selfora.dto.TherapistDtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapist")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('THERAPIST')")
public class TherapistController {

    @Autowired
    private TherapistService therapistService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<TherapistDashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(therapistService.getDashboardSummary());
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
