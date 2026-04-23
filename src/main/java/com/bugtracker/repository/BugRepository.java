package com.bugtracker.repository;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugPriority;
import com.bugtracker.model.BugStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByStatus(BugStatus status);

    List<Bug> findByPriority(BugPriority priority);

    List<Bug> findByAssignee(String assignee);

    List<Bug> findByTitleContainingIgnoreCase(String keyword);

    Page<Bug> findAll(Pageable pageable);

    Page<Bug> findByStatus(BugStatus status, Pageable pageable);

    Page<Bug> findByPriority(BugPriority priority, Pageable pageable);

    Page<Bug> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    long countByStatus(BugStatus status);

    long countByPriority(BugPriority priority);

    @Query("SELECT b.assignee, COUNT(b) FROM Bug b WHERE b.assignee IS NOT NULL GROUP BY b.assignee ORDER BY COUNT(b) DESC")
    List<Object[]> countByAssignee();
}
