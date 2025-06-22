package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.model.dto.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.SignUpRequestDTO;
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

    // just testing
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        return authService.authenticate(loginRequest)
                .map(response -> ResponseEntity.ok().body(response))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequest) {
        return authService.register(signUpRequest)
                .map(response -> ResponseEntity.ok().body(response))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
