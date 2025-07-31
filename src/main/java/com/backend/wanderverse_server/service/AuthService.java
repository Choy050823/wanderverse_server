package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.auth.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.auth.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.auth.SignUpRequestDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AuthService extends UserDetailsService {
    UserDetails loadUserByEmail(String email);

    AuthResponseDTO authenticate(LoginRequestDTO loginRequest);

    AuthResponseDTO register(SignUpRequestDTO signUpRequest);
}
