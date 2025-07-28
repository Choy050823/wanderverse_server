package com.backend.wanderverse_server.model.dto.auth;

import com.backend.wanderverse_server.model.dto.post.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDTO {
    private UserDTO user;
    private String token;
}
