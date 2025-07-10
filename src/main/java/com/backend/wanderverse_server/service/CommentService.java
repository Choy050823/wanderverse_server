package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.post.CommentDTO;
import com.backend.wanderverse_server.model.dto.post.CommentRequestDTO;
import com.backend.wanderverse_server.model.entity.post.CommentEntity;

import java.util.List;

public interface CommentService {
    CommentEntity createComment(CommentRequestDTO comment);

    List<CommentEntity> getCommentsByPost(Long postId);

    List<CommentDTO> getCommentTreeByPost(Long postId);
}
