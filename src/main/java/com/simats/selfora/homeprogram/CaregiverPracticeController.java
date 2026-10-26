package com.simats.selfora.homeprogram;

import com.simats.selfora.dto.CaregiverPracticeDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/home-practice", "/api/caregiver-practice"})
@CrossOrigin(origins = "*")
public class CaregiverPracticeController {

    @Autowired
    private CaregiverPracticeService caregiverPracticeService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('CAREGIVER') or hasRole('THERAPIST')")
    public ResponseEntity<CaregiverPracticeDtos.CaregiverPracticeSessionResponse> submitPractice(
            @RequestBody CaregiverPracticeDtos.SubmitPracticeRequest request) {
        return ResponseEntity.ok(caregiverPracticeService.submitPractice(request));
    }

    @PostMapping("/reviews/{sessionId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<CaregiverPracticeDtos.CaregiverPracticeSessionResponse> reviewPracticeSession(
            @PathVariable Long sessionId,
            @RequestBody CaregiverPracticeDtos.TherapistReviewRequest request) {
        return ResponseEntity.ok(caregiverPracticeService.reviewPracticeSession(sessionId, request));
    }

    @GetMapping("/pending-reviews")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<List<CaregiverPracticeDtos.CaregiverPracticeSessionResponse>> getPendingReviews() {
        return ResponseEntity.ok(caregiverPracticeService.getPendingReviewsForTherapist());
    }

    @GetMapping({"/history/{childId}", "/history/child/{childId}"})
    @PreAuthorize("hasRole('CAREGIVER') or hasRole('THERAPIST') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CaregiverPracticeDtos.CaregiverPracticeSessionResponse>> getPracticeHistory(@PathVariable Long childId) {
        return ResponseEntity.ok(caregiverPracticeService.getPracticeHistoryForChild(childId));
    }
}
