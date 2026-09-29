package com.simats.selfora.adaptation;

import com.simats.selfora.dto.AdaptivePlanDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/latest")
    public ResponseEntity<AdaptivePlanDtos.AdaptivePlanResponse> getLatestPlan(
            @RequestParam Long childId,
            @RequestParam Long activityId) {
        return ResponseEntity.ok(adaptivePlanService.getLatestAdaptivePlan(childId, activityId));
    }
}
