package com.backend.wanderverse_server.model.dto.post;

import com.backend.wanderverse_server.model.entity.post.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO {
    private long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likesCount;
    private int commentsCount;
    private PostType postType;
    private UserDTO creator;
    private DestinationDTO destination;
}
