package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.model.dto.auth.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.auth.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.auth.SignUpRequestDTO;
import com.backend.wanderverse_server.model.dto.post.UserDTO;
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.security.JwtUtil;
import com.backend.wanderverse_server.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    @Lazy
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private Mapper<UserEntity, UserDTO> userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username = email
        return userRepository.findUserByEmail(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.emptyList()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    // Load user by using email
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.emptyList()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public Optional<AuthResponseDTO> authenticate(LoginRequestDTO loginRequest) {
        try {
            // login using email and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate token
            String token = jwtUtil.generateToken(loginRequest.getEmail());

            // Return user with token
            return userRepository.findUserByEmail(loginRequest.getEmail())
                    .map(userEntity -> AuthResponseDTO.builder()
                        .user(userMapper.mapTo(userEntity))
                        .token(token)
                        .build());
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthResponseDTO> register(SignUpRequestDTO signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.error("User exists");
            throw new RuntimeException("User Already Exists!");
        }

        try {
            UserEntity userEntity = UserEntity.builder()
                    .username(signUpRequest.getUsername())
                    .email(signUpRequest.getEmail())
                    .password(encoder.encode(signUpRequest.getPassword()))
                    .description("")
                    .gamePoints(0)
                    .profilePicUrl("")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            UserEntity savedUserEntity = userRepository.save(userEntity);

            String token = jwtUtil.generateToken(signUpRequest.getEmail());

            log.info("successfully added user");
            return Optional.of(AuthResponseDTO.builder()
                    .user(userMapper.mapTo(savedUserEntity))
                    .token(token)
                    .build());
        } catch (Exception e) {
            log.error("Sign Up Failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
