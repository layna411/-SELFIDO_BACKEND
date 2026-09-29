package com.simats.selfora.activity;

import com.simats.selfora.dto.ActivityDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private TaskEngineService taskEngineService;

    @GetMapping
    public ResponseEntity<List<ActivityDtos.ActivityResponse>> getAllActivities() {
        return ResponseEntity.ok(taskEngineService.getAllActiveActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityDtos.ActivityResponse> getActivityById(@PathVariable Long id) {
        return ResponseEntity.ok(taskEngineService.getActivityById(id));
    }

    @GetMapping("/{id}/steps")
    public ResponseEntity<List<ActivityDtos.TaskStepResponse>> getTaskSteps(@PathVariable Long id) {
        return ResponseEntity.ok(taskEngineService.getTaskStepsForActivity(id));
    }
}
