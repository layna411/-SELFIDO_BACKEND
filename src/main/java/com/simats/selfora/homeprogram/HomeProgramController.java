package com.simats.selfora.homeprogram;

import com.simats.selfora.dto.HomeProgramDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home-programs")
@CrossOrigin(origins = "*")
public class HomeProgramController {

    @Autowired
    private HomeProgramService homeProgramService;

    @PostMapping
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<HomeProgramDtos.HomeProgramResponse> createHomeProgram(@RequestBody HomeProgramDtos.CreateHomeProgramRequest request) {
        return ResponseEntity.ok(homeProgramService.createHomeProgram(request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<HomeProgramDtos.HomeProgramResponse> updateProgramStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(homeProgramService.updateProgramStatus(id, status));
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<HomeProgramDtos.HomeProgramResponse> getActiveHomeProgramForChild(@PathVariable Long childId) {
        return ResponseEntity.ok(homeProgramService.getActiveHomeProgramForChild(childId));
    }

    @GetMapping("/child/{childId}/all")
    public ResponseEntity<List<HomeProgramDtos.HomeProgramResponse>> getAllHomeProgramsForChild(@PathVariable Long childId) {
        return ResponseEntity.ok(homeProgramService.getAllHomeProgramsForChild(childId));
    }

    @GetMapping("/today")
    public ResponseEntity<List<HomeProgramDtos.HomeProgramResponse>> getTodayHomePrograms() {
        return ResponseEntity.ok(homeProgramService.getHomeProgramsForCaregiver());
    }
}
