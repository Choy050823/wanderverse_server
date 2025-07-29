package com.backend.wanderverse_server.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDTO {
    private String postId;
    private String content;
    private String userId;
    private String parentCommentId;
}
