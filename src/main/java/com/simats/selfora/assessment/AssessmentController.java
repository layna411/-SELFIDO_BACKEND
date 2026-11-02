package com.simats.selfora.assessment;

import com.simats.selfora.dto.AssessmentDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments")
@CrossOrigin(origins = "*")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @PostMapping
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<AssessmentDtos.AssessmentResponse> createAssessment(@RequestBody AssessmentDtos.CreateAssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.createAssessment(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('THERAPIST') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AssessmentDtos.AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @GetMapping("/child/{childId}")
    @PreAuthorize("hasRole('THERAPIST') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<java.util.List<AssessmentDtos.AssessmentResponse>> getAssessmentsForChild(@PathVariable Long childId) {
        return ResponseEntity.ok(assessmentService.getAssessmentsForChild(childId));
    }

    @GetMapping("/child/{childId}/latest")
    @PreAuthorize("hasRole('THERAPIST') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AssessmentDtos.AssessmentResponse> getLatestAssessmentForChild(
            @PathVariable Long childId,
            @RequestParam(required = false) Long activityId) {
        return ResponseEntity.ok(assessmentService.getLatestAssessmentForChild(childId, activityId));
    }
}
