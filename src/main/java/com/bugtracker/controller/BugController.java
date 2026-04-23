package com.bugtracker.controller;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.service.BugService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bugs")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping
    public ResponseEntity<List<Bug>> getAllBugs(
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) BugPriority priority,
            @RequestParam(required = false) String search) {

        List<Bug> bugs;
        if (status != null) {
            bugs = bugService.getBugsByStatus(status);
        } else if (priority != null) {
            bugs = bugService.getBugsByPriority(priority);
        } else if (search != null && !search.isBlank()) {
            bugs = bugService.searchBugs(search);
        } else {
            bugs = bugService.getAllBugs();
        }
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bug> getBugById(@PathVariable Long id) {
        return ResponseEntity.ok(bugService.getBugById(id));
    }

    @PostMapping
    public ResponseEntity<Bug> createBug(@Valid @RequestBody Bug bug) {
        Bug created = bugService.createBug(bug);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bug> updateBug(@PathVariable Long id, @Valid @RequestBody Bug bug) {
        return ResponseEntity.ok(bugService.updateBug(id, bug));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBug(@PathVariable Long id) {
        bugService.deleteBug(id);
        return ResponseEntity.noContent().build();
    }
}
