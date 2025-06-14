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

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        return authService.authenticate(loginRequest)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequest) {
        return authService.register(signUpRequest)
            .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }
}
