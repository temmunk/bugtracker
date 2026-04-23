package com.bugtracker.controller;

import com.bugtracker.model.ActivityLog;
import com.bugtracker.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getRecentActivity() {
        return ResponseEntity.ok(activityLogService.getRecentActivity());
    }

    @GetMapping("/bug/{bugId}")
    public ResponseEntity<List<ActivityLog>> getActivityByBugId(@PathVariable Long bugId) {
        return ResponseEntity.ok(activityLogService.getLogsByBugId(bugId));
    }
}
