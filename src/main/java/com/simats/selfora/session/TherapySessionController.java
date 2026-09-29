package com.simats.selfora.session;

import com.simats.selfora.dto.SessionDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class TherapySessionController {

    @Autowired
    private TherapySessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<SessionDtos.SessionResponse> startSession(@RequestBody SessionDtos.StartSessionRequest request) {
        return ResponseEntity.ok(sessionService.startSession(request));
    }

    @PostMapping("/{id}/performance")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('THERAPIST', 'ADMIN')")
    public ResponseEntity<SessionDtos.PerformanceRecordResponse> recordPerformance(
            @PathVariable Long id,
            @RequestBody SessionDtos.RecordPerformanceRequest request) {
        return ResponseEntity.ok(sessionService.recordPerformance(id, request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SessionDtos.SessionSummaryResponse> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.completeSession(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDtos.SessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }
}
