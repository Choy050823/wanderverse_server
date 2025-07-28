package com.backend.wanderverse_server.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
