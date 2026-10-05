package com.simats.selfora.child;

import com.simats.selfora.dto.ChildModeDtos.*;
import com.simats.selfora.entity.ChildActivityProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/child-mode")
@CrossOrigin(origins = "*")
public class ChildModeController {

    @Autowired
    private ChildModeService childModeService;

    @GetMapping("/dashboard/{childId}")
    public ResponseEntity<ChildDashboardResponse> getDashboard(@PathVariable Long childId) {
        return ResponseEntity.ok(childModeService.getChildDashboard(childId));
    }

    @PostMapping("/progress")
    public ResponseEntity<ChildActivityProgress> recordProgress(@RequestBody RecordStepProgressRequest request) {
        return ResponseEntity.ok(childModeService.recordStepProgress(request));
    }

    @GetMapping("/rewards/{childId}")
    public ResponseEntity<List<ChildRewardDto>> getRewards(@PathVariable Long childId) {
        return ResponseEntity.ok(childModeService.getChildRewards(childId));
    }

    @GetMapping("/preferences/{childId}")
    public ResponseEntity<ChildModePreferenceDto> getPreferences(@PathVariable Long childId) {
        return ResponseEntity.ok(childModeService.getChildPreferences(childId));
    }

    @PutMapping("/preferences/{childId}")
    public ResponseEntity<ChildModePreferenceDto> updatePreferences(@PathVariable Long childId, @RequestBody ChildModePreferenceDto dto) {
        return ResponseEntity.ok(childModeService.updateChildPreferences(childId, dto));
    }
}
