package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.entity.UserEntity;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/login")
    public String authenticateUser(@RequestBody UserEntity userEntity) {
        // login using email and password
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    userEntity.getEmail(),
                    userEntity.getPassword()
            )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // NOTE: here the username is email since we use email to log in
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    @PostMapping("/signup")
    public String registerUser(@RequestBody UserEntity userEntity) {
        if (userRepository.existsByEmail(userEntity.getEmail())) {
            return "Error: User email already exists";
        }

        if(userRepository.existsByEmail(userEntity.getUsername())) {
            return "Error: Username is already taken";
        }

        // Create new user account
        userEntity.setPassword(encoder.encode(userEntity.getPassword()));
        userEntity.setGamePoints(0);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        return "User registered successfully!";
    }
}
