package com.backend.wanderverse_server.model.dto;

import com.backend.wanderverse_server.model.entity.CommentEntity;
import com.backend.wanderverse_server.model.entity.PostEntity;
import com.backend.wanderverse_server.model.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentDTO {
    private long id;
    private String content;
    private PostEntity post;
    private UserEntity user;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;
}
