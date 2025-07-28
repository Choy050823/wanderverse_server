package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.auth.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.auth.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.auth.SignUpRequestDTO;
import com.backend.wanderverse_server.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // just testing hope work sss
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        return authService.authenticate(loginRequest)
                .map(response -> ResponseEntity.ok().body(response))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequest) {
        try {
            return authService.register(signUpRequest)
                    .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                    .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }
}
