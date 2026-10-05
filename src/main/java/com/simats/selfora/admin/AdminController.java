package com.simats.selfora.admin;

import com.simats.selfora.dto.AdminDtos;
import com.simats.selfora.entity.AuditLog;
import com.simats.selfora.entity.Child;
import com.simats.selfora.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDtos.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/charts")
    public ResponseEntity<AdminDtos.ChartData> getChartData() {
        return ResponseEntity.ok(adminService.getChartData());
    }

    @GetMapping("/therapists")
    public ResponseEntity<List<User>> getAllTherapists() {
        return ResponseEntity.ok(adminService.getAllTherapists());
    }

    @PostMapping("/therapists")
    public ResponseEntity<User> createTherapist(@RequestBody AdminDtos.TherapistCreateRequest request) {
        return ResponseEntity.ok(adminService.createTherapist(request, 4L));
    }

    @PutMapping("/therapists/{id}/status")
    public ResponseEntity<User> updateTherapistStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        return ResponseEntity.ok(adminService.updateTherapistStatus(id, isActive, 4L));
    }

    @GetMapping("/caregivers")
    public ResponseEntity<List<User>> getAllCaregivers() {
        return ResponseEntity.ok(adminService.getAllCaregivers());
    }

    @PutMapping("/caregivers/{id}/status")
    public ResponseEntity<User> updateCaregiverStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        return ResponseEntity.ok(adminService.updateCaregiverStatus(id, isActive, 4L));
    }

    @PostMapping("/therapists/{id}/reset-password")
    public ResponseEntity<Void> resetTherapistPassword(@PathVariable Long id, @RequestParam(required = false) String newPassword) {
        adminService.resetTherapistPassword(id, newPassword, 4L);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/children")
    public ResponseEntity<List<Child>> getAllChildren() {
        return ResponseEntity.ok(adminService.getAllChildren());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
