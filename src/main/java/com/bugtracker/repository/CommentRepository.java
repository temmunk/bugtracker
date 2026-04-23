package com.bugtracker.repository;

import com.bugtracker.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByBugIdOrderByCreatedAtDesc(Long bugId);

    long countByBugId(Long bugId);
}
