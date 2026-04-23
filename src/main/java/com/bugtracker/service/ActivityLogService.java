package com.bugtracker.service;

import com.bugtracker.model.ActivityLog;
import com.bugtracker.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(Long bugId, String action, String details, String performedBy) {
        ActivityLog entry = new ActivityLog(bugId, action, details, performedBy);
        activityLogRepository.save(entry);
    }

    public List<ActivityLog> getLogsByBugId(Long bugId) {
        return activityLogRepository.findByBugIdOrderByTimestampDesc(bugId);
    }

    public List<ActivityLog> getRecentActivity() {
        return activityLogRepository.findTop20ByOrderByTimestampDesc();
    }
}
