package com.bugtracker.service;

import com.bugtracker.model.Bug;
import com.bugtracker.model.Comment;
import com.bugtracker.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BugService bugService;

    public CommentService(CommentRepository commentRepository, BugService bugService) {
        this.commentRepository = commentRepository;
        this.bugService = bugService;
    }

    public List<Comment> getCommentsByBugId(Long bugId) {
        bugService.getBugById(bugId);
        return commentRepository.findByBugIdOrderByCreatedAtDesc(bugId);
    }

    public Comment addComment(Long bugId, Comment comment) {
        Bug bug = bugService.getBugById(bugId);
        comment.setBug(bug);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        commentRepository.delete(comment);
    }
}
