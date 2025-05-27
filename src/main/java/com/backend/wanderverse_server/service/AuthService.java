package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.SignUpRequestDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AuthService extends UserDetailsService {
    UserDetails loadUserByEmail(String email);

    Optional<AuthResponseDTO> authenticate(LoginRequestDTO loginRequest);

    Optional<AuthResponseDTO> register(SignUpRequestDTO signUpRequest);
}
