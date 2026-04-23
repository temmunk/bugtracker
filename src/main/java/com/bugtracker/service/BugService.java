package com.bugtracker.service;

import com.bugtracker.exception.BugNotFoundException;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import com.bugtracker.repository.BugRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BugService {

    private final BugRepository bugRepository;

    public BugService(BugRepository bugRepository) {
        this.bugRepository = bugRepository;
    }

    public List<Bug> getAllBugs() {
        return bugRepository.findAll();
    }

    public Bug getBugById(Long id) {
        return bugRepository.findById(id)
                .orElseThrow(() -> new BugNotFoundException(id));
    }

    public Bug createBug(Bug bug) {
        if (bug.getStatus() == null) {
            bug.setStatus(BugStatus.OPEN);
        }
        return bugRepository.save(bug);
    }

    public Bug updateBug(Long id, Bug bugDetails) {
        Bug existing = getBugById(id);
        existing.setTitle(bugDetails.getTitle());
        existing.setDescription(bugDetails.getDescription());
        existing.setPriority(bugDetails.getPriority());
        existing.setStatus(bugDetails.getStatus());
        existing.setReporter(bugDetails.getReporter());
        existing.setAssignee(bugDetails.getAssignee());
        return bugRepository.save(existing);
    }

    public void deleteBug(Long id) {
        Bug bug = getBugById(id);
        bugRepository.delete(bug);
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
}
