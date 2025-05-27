package com.backend.wanderverse_server.model.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private long id;
    private String username;
    private String email;
//    private String password;
    private String description;
    private String profilePicUrl;
    private int gamePoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
