package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.post.CommentDTO;
import com.backend.wanderverse_server.model.dto.post.CommentRequestDTO;
import com.backend.wanderverse_server.model.entity.post.CommentEntity;
import com.backend.wanderverse_server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@RequestBody CommentRequestDTO comment) {
        CommentEntity savedComment = commentService.createComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommentDTO.builder()
                        .id(savedComment.getId())
                        .content(savedComment.getContent())
                        .post(savedComment.getPost())
                        .user(savedComment.getUser())
                        .createdAt(savedComment.getCreatedAt())
                        .replies(List.of()).build()
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok().body(commentService.getCommentTreeByPost(postId));
    }
}
