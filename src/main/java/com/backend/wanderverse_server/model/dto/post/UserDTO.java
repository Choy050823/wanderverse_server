package com.backend.wanderverse_server.model.dto.post;

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
public class UserDTO {

    private long id;
    private String username;
    private String email;
    private String description;
    private String profilePicUrl;
    private int gamePoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> badgesUrls;
}
