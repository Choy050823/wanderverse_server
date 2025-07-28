package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.auth.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.auth.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.auth.SignUpRequestDTO;
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.model.mappers.Mapper;
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.security.JwtUtil; // Assuming your JWT utility class
import com.backend.wanderverse_server.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService; // The service we are testing

    @Mock
    private UserRepository userRepository; // Mocked dependency
    @Mock
    private PasswordEncoder passwordEncoder; // Mocked dependency
    @Mock
    private JwtUtil jwtUtil; // Mocked dependency
    @Mock
    private AuthenticationManager authenticationManager; // Mocked dependency
    @Mock
    private Mapper userMapper; // Mocked DTO mapper

    private UserEntity testUserEntity;
    private SignUpRequestDTO signUpRequestDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        // Initialize common test data
        testUserEntity = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword") // This would be the encoded password
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        signUpRequestDTO = new SignUpRequestDTO("newuser@gmail.com", "newuser", "password123");
        loginRequestDTO = new LoginRequestDTO("test@example.com", "password123");
    }

    @Test
    void register_success() {
        // Mock behaviors of dependencies for a successful registration
        when(userRepository.existsByEmail(signUpRequestDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signUpRequestDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity); // Return a saved user with an ID
        when(jwtUtil.generateToken(testUserEntity.getEmail())).thenReturn("mockJwtToken");
        when(userMapper.mapTo(any(UserEntity.class))).thenReturn(AuthResponseDTO.builder().token("mockJwtToken").build());

        // Call the service method
        Optional<AuthResponseDTO> response = authService.register(signUpRequestDTO);

        // Assertions
        assertTrue(response.isPresent());
        assertEquals("mockJwtToken", response.get().getToken());

        // Verify interactions with mocks
        verify(userRepository, times(1)).existsByEmail(signUpRequestDTO.getEmail());
        verify(passwordEncoder, times(1)).encode(signUpRequestDTO.getPassword());
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(jwtUtil, times(1)).generateToken(testUserEntity.getEmail());
        verify(userMapper, times(1)).mapTo(any(UserEntity.class));
    }

    @Test
    void register_userAlreadyExists_throwsException() {
        // Mock behavior for an existing user
        when(userRepository.existsByEmail(signUpRequestDTO.getEmail())).thenReturn(true);

        // Call the service method and expect an exception
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            authService.register(signUpRequestDTO);
        });

        // Assertions
        assertEquals("User Already Exists!", thrown.getMessage());

        // Verify interactions (should not proceed to encode/save)
        verify(userRepository, times(1)).existsByEmail(signUpRequestDTO.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtUtil, never()).generateToken(anyString());
        verify(userMapper, never()).mapTo(any(UserEntity.class));
    }

    @Test
    void login_success() {
        // Mock behavior for a successful login
        Authentication authentication = mock(Authentication.class); // Mock the Authentication object
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mock(UserDetails.class)); // Mock UserDetails
        when(((UserDetails)authentication.getPrincipal()).getUsername()).thenReturn(testUserEntity.getEmail());

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(testUserEntity.getEmail())).thenReturn("mockJwtToken");
        when(userRepository.findUserByEmail(testUserEntity.getEmail())).thenReturn(Optional.of(testUserEntity));
        when(userMapper.mapTo(any(UserEntity.class))).thenReturn(AuthResponseDTO.builder().token("mockJwtToken").build());

        // Call the service method
        Optional<AuthResponseDTO> response = authService.authenticate(loginRequestDTO);

        // Assertions
        assertTrue(response.isPresent());
        assertEquals("mockJwtToken", response.get().getToken());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        verify(jwtUtil, times(1)).generateToken(testUserEntity.getEmail());
        verify(userRepository, times(1)).findUserByEmail(testUserEntity.getEmail());
        verify(userMapper, times(1)).mapTo(any(UserEntity.class));
    }

    @Test
    void login_invalidCredentials_throwsException() {
        // Mock behavior for invalid credentials
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Call the service method and expect an exception
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequestDTO);
        });

        // Assertions
        assertEquals("Invalid credentials", thrown.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        verify(jwtUtil, never()).generateToken(anyString());
        verify(userRepository, never()).findUserByEmail(anyString());
        verify(userMapper, never()).mapTo(any(UserEntity.class));
    }

    @Test
    void login_userNotFoundAfterAuthentication_throwsException() {
        // Mock successful authentication but user not found in repo (edge case)
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mock(UserDetails.class));
        when(((UserDetails)authentication.getPrincipal()).getUsername()).thenReturn(loginRequestDTO.getEmail());

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())))
                .thenReturn(authentication);
        when(userRepository.findUserByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequestDTO);
        });

        assertEquals("User not found after authentication", thrown.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findUserByEmail(loginRequestDTO.getEmail());
        verify(jwtUtil, never()).generateToken(anyString());
        verify(userMapper, never()).mapTo(any(UserEntity.class));
    }
}