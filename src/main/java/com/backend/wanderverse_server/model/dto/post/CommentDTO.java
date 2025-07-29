package com.backend.wanderverse_server.model.dto.post;

import com.backend.wanderverse_server.model.entity.post.PostEntity;
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    private long id;
    private String content;
    private PostEntity post;
    private UserEntity user;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;
}
