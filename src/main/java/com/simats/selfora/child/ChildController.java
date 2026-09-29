package com.simats.selfora.child;

import com.simats.selfora.dto.ChildDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children")
@CrossOrigin(origins = "*")
public class ChildController {

    @Autowired
    private ChildService childService;

    @GetMapping
    public ResponseEntity<List<ChildDto>> getMyChildren() {
        return ResponseEntity.ok(childService.getAssignedChildrenForCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChildDto> getChildById(@PathVariable Long id) {
        return ResponseEntity.ok(childService.getChildById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<ChildDto> createChild(@RequestBody ChildDto dto) {
        return ResponseEntity.ok(childService.createChild(dto));
    }
}
