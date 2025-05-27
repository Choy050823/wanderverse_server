package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.SignUpRequestDTO;
import com.backend.wanderverse_server.model.dto.UserDTO;
import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.security.JwtUtil;
import lombok.extern.java.Log;
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
@Log
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
                        .userDTO(userMapper.mapTo(userEntity))
                        .token(token)
                        .build());
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthResponseDTO> register(SignUpRequestDTO signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            System.out.println("User exists");
            return Optional.empty();
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

            System.out.println("successfully added user");
            return Optional.of(AuthResponseDTO.builder()
                    .userDTO(userMapper.mapTo(savedUserEntity))
                    .token(token)
                    .build());
        } catch (Exception e) {
            System.out.println("Sign Up Failed: " + e.getMessage());
            return Optional.empty();
        }
    }
}
