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
import com.backend.wanderverse_server.util.exceptions.LoginFailedException;
import com.backend.wanderverse_server.util.exceptions.SignupFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

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
    public AuthResponseDTO authenticate(LoginRequestDTO loginRequest) {
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
                        .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication: " + loginRequest.getEmail()));
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email {}", loginRequest.getEmail());
            // More specific exception for bad password/email combo
            throw new LoginFailedException("Invalid email or password provided.", e);
        } catch (UsernameNotFoundException e) {
            log.warn("Login attempt for non-existent user: {}", loginRequest.getEmail());
            // This case should be caught by BadCredentialsException often, but explicit is fine
            throw new LoginFailedException("Invalid email or password provided.", e);
        } catch (Exception e) { // Catch any other unexpected authentication issues
            log.error("An unexpected error occurred during authentication for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new LoginFailedException("Authentication failed due to an unexpected error.", e);
        }
    }

    @Override
    public AuthResponseDTO register(SignUpRequestDTO signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("Sign-up attempt for existing user: {}", signUpRequest.getEmail());
            throw new SignupFailedException("User with email '" + signUpRequest.getEmail() + "' already exists!");
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
            return AuthResponseDTO.builder()
                    .user(userMapper.mapTo(savedUserEntity))
                    .token(token)
                    .build();
        } catch (DataIntegrityViolationException e) { // Example: if there's another unique constraint not caught by existsByEmail
            log.error("Data integrity violation during sign-up for {}: {}", signUpRequest.getEmail(), e.getMessage(), e);
            throw new SignupFailedException("Sign-up failed due to a data conflict. Please try again.", e);
        } catch (Exception e) { // Catch any other unexpected issues during save
            log.error("An unexpected error occurred during sign-up for {}: {}", signUpRequest.getEmail(), e.getMessage(), e);
            throw new SignupFailedException("Sign-up failed due to an unexpected error.", e);
        }
    }
}
