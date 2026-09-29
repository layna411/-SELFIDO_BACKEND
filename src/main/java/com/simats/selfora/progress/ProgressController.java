package com.simats.selfora.progress;

import com.simats.selfora.dto.ProgressDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/children")
@CrossOrigin(origins = "*")
public class ProgressController {

    @Autowired
    private ProgressAnalyticsService progressAnalyticsService;

    @GetMapping("/{childId}/progress")
    public ResponseEntity<ProgressDtos.ProgressSummaryResponse> getChildProgress(
            @PathVariable Long childId,
            @RequestParam(defaultValue = "1") Long activityId) {
        return ResponseEntity.ok(progressAnalyticsService.getProgressSummary(childId, activityId));
    }
}
