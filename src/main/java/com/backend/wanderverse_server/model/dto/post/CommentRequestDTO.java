package com.backend.wanderverse_server.model.dto.post;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentRequestDTO {
    private String postId;
    private String content;
    private String userId;
    private String parentCommentId;
}
