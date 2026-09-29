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
    public ResponseEntity<AssessmentDtos.AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }
}
