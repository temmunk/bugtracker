package com.bugtracker.dto;

import java.util.Map;

public class DashboardStats {

    private long totalBugs;
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private Map<String, Long> topAssignees;

    public DashboardStats() {}

    public DashboardStats(long totalBugs, Map<String, Long> byStatus,
                          Map<String, Long> byPriority, Map<String, Long> topAssignees) {
        this.totalBugs = totalBugs;
        this.byStatus = byStatus;
        this.byPriority = byPriority;
        this.topAssignees = topAssignees;
    }

    public long getTotalBugs() { return totalBugs; }
    public void setTotalBugs(long totalBugs) { this.totalBugs = totalBugs; }

    public Map<String, Long> getByStatus() { return byStatus; }
    public void setByStatus(Map<String, Long> byStatus) { this.byStatus = byStatus; }

    public Map<String, Long> getByPriority() { return byPriority; }
    public void setByPriority(Map<String, Long> byPriority) { this.byPriority = byPriority; }

    public Map<String, Long> getTopAssignees() { return topAssignees; }
    public void setTopAssignees(Map<String, Long> topAssignees) { this.topAssignees = topAssignees; }
}
