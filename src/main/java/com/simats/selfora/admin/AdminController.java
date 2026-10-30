package com.simats.selfora.admin;

import com.simats.selfora.dto.AdminDtos;
import com.simats.selfora.entity.AuditLog;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.User;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') || hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElse(1L);
        } catch (Exception e) {
            return 1L;
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDtos.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/charts")
    public ResponseEntity<AdminDtos.ChartData> getChartData() {
        return ResponseEntity.ok(adminService.getChartData());
    }

    @GetMapping("/therapists")
    public ResponseEntity<List<AdminDtos.TherapistProfile>> getAllTherapists() {
        return ResponseEntity.ok(adminService.getAllTherapists());
    }

    @GetMapping("/therapists/{id}")
    public ResponseEntity<AdminDtos.TherapistProfile> getTherapistById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getTherapistById(id));
    }

    @PostMapping("/therapists")
    public ResponseEntity<AdminDtos.TherapistProfile> createTherapist(@RequestBody AdminDtos.TherapistCreateRequest request) {
        return ResponseEntity.ok(adminService.createTherapist(request, getCurrentUserId()));
    }

    @PutMapping("/therapists/{id}")
    public ResponseEntity<AdminDtos.TherapistProfile> updateTherapist(@PathVariable Long id, @RequestBody AdminDtos.TherapistUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateTherapist(id, request, getCurrentUserId()));
    }

    @PutMapping("/therapists/{id}/status")
    public ResponseEntity<AdminDtos.TherapistProfile> updateTherapistStatus(
            @PathVariable Long id,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "isActive", required = false) Boolean isActiveParam
    ) {
        boolean status = active != null ? active : (isActiveParam != null ? isActiveParam : true);
        return ResponseEntity.ok(adminService.updateTherapistStatus(id, status, getCurrentUserId()));
    }

    @GetMapping("/caregivers")
    public ResponseEntity<List<AdminDtos.CaregiverProfile>> getAllCaregivers() {
        return ResponseEntity.ok(adminService.getAllCaregivers());
    }

    @GetMapping("/caregivers/{id}")
    public ResponseEntity<AdminDtos.CaregiverProfile> getCaregiverById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCaregiverById(id));
    }

    @PostMapping("/caregivers")
    public ResponseEntity<AdminDtos.CaregiverProfile> createCaregiver(@RequestBody AdminDtos.CaregiverCreateRequest request) {
        return ResponseEntity.ok(adminService.createCaregiver(request, getCurrentUserId()));
    }

    @PutMapping("/caregivers/{id}")
    public ResponseEntity<AdminDtos.CaregiverProfile> updateCaregiver(@PathVariable Long id, @RequestBody AdminDtos.CaregiverUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateCaregiver(id, request, getCurrentUserId()));
    }

    @PutMapping("/caregivers/{id}/status")
    public ResponseEntity<AdminDtos.CaregiverProfile> updateCaregiverStatus(
            @PathVariable Long id,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "isActive", required = false) Boolean isActiveParam
    ) {
        boolean status = active != null ? active : (isActiveParam != null ? isActiveParam : true);
        return ResponseEntity.ok(adminService.updateCaregiverStatus(id, status, getCurrentUserId()));
    }

    @PostMapping("/caregivers/{id}/reset-password")
    public ResponseEntity<Void> resetCaregiverPassword(@PathVariable Long id, @RequestBody(required = false) AdminDtos.ResetPasswordRequestBody request) {
        adminService.resetCaregiverPassword(id, request != null ? request.getNewPassword() : null, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/therapists/{id}/reset-password")
    public ResponseEntity<Void> resetTherapistPassword(@PathVariable Long id, @RequestParam(required = false) String newPassword) {
        adminService.resetTherapistPassword(id, newPassword, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/children")
    public ResponseEntity<List<AdminDtos.AdminChildSummary>> getAllChildren() {
        return ResponseEntity.ok(adminService.getAllChildrenForAdmin());
    }

    @GetMapping("/children/{id}")
    public ResponseEntity<AdminDtos.AdminChildDetailProfile> getChildById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getChildDetailProfileForAdmin(id));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
