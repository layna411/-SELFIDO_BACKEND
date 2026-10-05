package com.simats.selfora.adaptation;

import com.simats.selfora.dto.AdaptivePlanDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adaptive-plans")
@CrossOrigin(origins = "*")
public class AdaptivePlanController {

    @Autowired
    private AdaptivePlanService adaptivePlanService;

    @PostMapping
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<AdaptivePlanDtos.AdaptivePlanResponse> createAdaptivePlan(@RequestBody AdaptivePlanDtos.CreateAdaptivePlanRequest request) {
        return ResponseEntity.ok(adaptivePlanService.createOrConfirmAdaptivePlan(request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<AdaptivePlanDtos.AdaptivePlanResponse> approveAdaptivePlan(@PathVariable Long id) {
        return ResponseEntity.ok(adaptivePlanService.approveAdaptivePlan(id));
    }

    @GetMapping("/latest")
    public ResponseEntity<AdaptivePlanDtos.AdaptivePlanResponse> getLatestPlan(
            @RequestParam Long childId,
            @RequestParam Long activityId) {
        return ResponseEntity.ok(adaptivePlanService.getLatestAdaptivePlan(childId, activityId));
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<List<AdaptivePlanDtos.AdaptivePlanResponse>> getAllPlansForChild(@PathVariable Long childId) {
        return ResponseEntity.ok(adaptivePlanService.getAllPlansForChild(childId));
    }
}
