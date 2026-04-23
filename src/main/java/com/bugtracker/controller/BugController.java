package com.bugtracker.controller;

import com.bugtracker.dto.DashboardStats;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.service.BugService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/bugs")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping
    public ResponseEntity<Page<Bug>> getAllBugs(
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) BugPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Bug> bugs;
        if (status != null) {
            bugs = bugService.getBugsByStatusPaged(status, pageable);
        } else if (priority != null) {
            bugs = bugService.getBugsByPriorityPaged(priority, pageable);
        } else if (search != null && !search.isBlank()) {
            bugs = bugService.searchBugsPaged(search, pageable);
        } else {
            bugs = bugService.getAllBugsPaged(pageable);
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

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(bugService.getDashboardStats());
    }

    @GetMapping("/export")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=bugs_export.csv");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Bug> bugs = bugService.getAllBugsForExport();

        PrintWriter writer = response.getWriter();
        writer.println("ID,Title,Description,Priority,Status,Reporter,Assignee,Created,Updated");

        for (Bug bug : bugs) {
            writer.printf("%d,\"%s\",\"%s\",%s,%s,\"%s\",\"%s\",%s,%s%n",
                    bug.getId(),
                    escapeCsv(bug.getTitle()),
                    escapeCsv(bug.getDescription()),
                    bug.getPriority(),
                    bug.getStatus(),
                    escapeCsv(bug.getReporter()),
                    escapeCsv(bug.getAssignee()),
                    bug.getCreatedAt() != null ? bug.getCreatedAt().format(fmt) : "",
                    bug.getUpdatedAt() != null ? bug.getUpdatedAt().format(fmt) : "");
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
