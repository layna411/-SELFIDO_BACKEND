package com.simats.selfora.adaptation;

import com.simats.selfora.dto.PromptFadingDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prompt-fading")
@CrossOrigin(origins = "*")
public class PromptFadingController {

    @Autowired
    private PromptFadingService promptFadingService;

    @PostMapping("/plans")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<PromptFadingDtos.PromptFadingPlanResponse> createPromptFadingPlan(
            @RequestBody PromptFadingDtos.CreatePromptFadingPlanRequest request) {
        return ResponseEntity.ok(promptFadingService.createPromptFadingPlan(request));
    }

    @PostMapping("/plans/{planId}/approve")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<PromptFadingDtos.PromptFadingPlanResponse> approvePromptFadingPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(promptFadingService.approvePromptFadingPlan(planId));
    }

    @PutMapping("/plans/{planId}/status")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<PromptFadingDtos.PromptFadingPlanResponse> updatePlanStatus(
            @PathVariable Long planId,
            @RequestParam String status) {
        return ResponseEntity.ok(promptFadingService.updatePlanStatus(planId, status));
    }

    @GetMapping("/child/{childId}/plans")
    public ResponseEntity<List<PromptFadingDtos.PromptFadingPlanResponse>> getFadingPlansForChild(@PathVariable Long childId) {
        return ResponseEntity.ok(promptFadingService.getFadingPlansForChild(childId));
    }

    @GetMapping("/child/{childId}/history")
    public ResponseEntity<List<PromptFadingDtos.PromptFadingHistoryItemResponse>> getFadingHistoryForChild(@PathVariable Long childId) {
        return ResponseEntity.ok(promptFadingService.getFadingHistoryForChild(childId));
    }
}
