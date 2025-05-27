package com.backend.wanderverse_server.model.dto;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

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
