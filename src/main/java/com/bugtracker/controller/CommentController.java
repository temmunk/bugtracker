package com.bugtracker.controller;

import com.bugtracker.model.Comment;
import com.bugtracker.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bugs/{bugId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long bugId) {
        return ResponseEntity.ok(commentService.getCommentsByBugId(bugId));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable Long bugId,
                                              @Valid @RequestBody Comment comment) {
        Comment created = commentService.addComment(bugId, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long bugId,
                                              @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
