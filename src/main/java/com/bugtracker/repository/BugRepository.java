package com.bugtracker.repository;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByStatus(BugStatus status);

    List<Bug> findByPriority(BugPriority priority);

    List<Bug> findByAssignee(String assignee);

    List<Bug> findByTitleContainingIgnoreCase(String keyword);
}
