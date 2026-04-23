package com.bugtracker.service;

import com.bugtracker.dto.DashboardStats;
import com.bugtracker.exception.BugNotFoundException;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.repository.BugRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BugService {

    private final BugRepository bugRepository;
    private final ActivityLogService activityLogService;

    public BugService(BugRepository bugRepository, ActivityLogService activityLogService) {
        this.bugRepository = bugRepository;
        this.activityLogService = activityLogService;
    }

    public List<Bug> getAllBugs() {
        return bugRepository.findAll();
    }

    public Page<Bug> getAllBugsPaged(Pageable pageable) {
        return bugRepository.findAll(pageable);
    }

    public Page<Bug> getBugsByStatusPaged(BugStatus status, Pageable pageable) {
        return bugRepository.findByStatus(status, pageable);
    }

    public Page<Bug> getBugsByPriorityPaged(BugPriority priority, Pageable pageable) {
        return bugRepository.findByPriority(priority, pageable);
    }

    public Page<Bug> searchBugsPaged(String keyword, Pageable pageable) {
        return bugRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Bug getBugById(Long id) {
        return bugRepository.findById(id)
                .orElseThrow(() -> new BugNotFoundException(id));
    }

    public Bug createBug(Bug bug) {
        if (bug.getStatus() == null) {
            bug.setStatus(BugStatus.OPEN);
        }
        Bug saved = bugRepository.save(bug);
        activityLogService.log(saved.getId(), "CREATED",
                "Bug created: " + saved.getTitle(), saved.getReporter());
        return saved;
    }

    public Bug updateBug(Long id, Bug bugDetails) {
        Bug existing = getBugById(id);

        StringBuilder changes = new StringBuilder();
        if (!existing.getStatus().equals(bugDetails.getStatus())) {
            changes.append("Status: ").append(existing.getStatus()).append(" → ").append(bugDetails.getStatus()).append("; ");
        }
        if (!existing.getPriority().equals(bugDetails.getPriority())) {
            changes.append("Priority: ").append(existing.getPriority()).append(" → ").append(bugDetails.getPriority()).append("; ");
        }
        if (!nullSafeEquals(existing.getAssignee(), bugDetails.getAssignee())) {
            changes.append("Assignee: ").append(orEmpty(existing.getAssignee())).append(" → ").append(orEmpty(bugDetails.getAssignee())).append("; ");
        }

        existing.setTitle(bugDetails.getTitle());
        existing.setDescription(bugDetails.getDescription());
        existing.setPriority(bugDetails.getPriority());
        existing.setStatus(bugDetails.getStatus());
        existing.setReporter(bugDetails.getReporter());
        existing.setAssignee(bugDetails.getAssignee());
        Bug saved = bugRepository.save(existing);

        String detail = changes.length() > 0 ? changes.toString().trim() : "Bug updated";
        activityLogService.log(id, "UPDATED", detail, bugDetails.getReporter());
        return saved;
    }

    public void deleteBug(Long id) {
        Bug bug = getBugById(id);
        bugRepository.delete(bug);
        activityLogService.log(id, "DELETED", "Bug deleted: " + bug.getTitle(), null);
    }

    private boolean nullSafeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private String orEmpty(String s) {
        return s == null ? "(none)" : s;
    }

    public List<Bug> getBugsByStatus(BugStatus status) {
        return bugRepository.findByStatus(status);
    }

    public List<Bug> getBugsByPriority(BugPriority priority) {
        return bugRepository.findByPriority(priority);
    }

    public List<Bug> searchBugs(String keyword) {
        return bugRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public DashboardStats getDashboardStats() {
        long total = bugRepository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (BugStatus s : BugStatus.values()) {
            byStatus.put(s.name(), bugRepository.countByStatus(s));
        }

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (BugPriority p : BugPriority.values()) {
            byPriority.put(p.name(), bugRepository.countByPriority(p));
        }

        Map<String, Long> topAssignees = new LinkedHashMap<>();
        bugRepository.countByAssignee().stream()
                .limit(5)
                .forEach(row -> topAssignees.put((String) row[0], (Long) row[1]));

        return new DashboardStats(total, byStatus, byPriority, topAssignees);
    }

    public List<Bug> getAllBugsForExport() {
        return bugRepository.findAll();
    }
}
