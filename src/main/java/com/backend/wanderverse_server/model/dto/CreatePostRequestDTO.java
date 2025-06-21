package com.backend.wanderverse_server.model.dto;

import com.backend.wanderverse_server.model.entity.PostType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreatePostRequestDTO {
    // when frontend send here it is in string format
    private String title;
    private String content;
    private List<String> imageUrls;
    private String creatorId;
    private String destinationId;
    private String postType;
}
