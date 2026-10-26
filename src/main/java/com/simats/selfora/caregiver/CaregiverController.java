package com.simats.selfora.caregiver;

import com.simats.selfora.dto.CaregiverDtos.CaregiverDashboardSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caregiver")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('CAREGIVER', 'SUPER_ADMIN', 'ADMIN') || hasAnyAuthority('ROLE_CAREGIVER', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
public class CaregiverController {

    @Autowired
    private CaregiverService caregiverService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<CaregiverDashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(caregiverService.getDashboardSummary());
    }

    @GetMapping("/children/{childId}/validate-access")
    public ResponseEntity<Boolean> validateChildAccess(@PathVariable Long childId) {
        return ResponseEntity.ok(caregiverService.validateCaregiverChildAccess(childId));
    }
}
