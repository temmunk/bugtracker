package com.bugtracker.repository;

import com.bugtracker.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByBugIdOrderByTimestampDesc(Long bugId);

    List<ActivityLog> findTop20ByOrderByTimestampDesc();
}
