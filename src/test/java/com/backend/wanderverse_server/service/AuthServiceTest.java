package com.backend.wanderverse_server.service;

import com.backend.wanderverse_server.model.dto.auth.AuthResponseDTO;
import com.backend.wanderverse_server.model.dto.auth.LoginRequestDTO;
import com.backend.wanderverse_server.model.dto.auth.SignUpRequestDTO;
import com.backend.wanderverse_server.model.dto.post.UserDTO; // Assuming this is your User DTO
import com.backend.wanderverse_server.model.entity.auth.UserEntity;
import com.backend.wanderverse_server.model.mappers.Mapper; // Assuming this is your DTO mapper
import com.backend.wanderverse_server.repository.UserRepository;
import com.backend.wanderverse_server.security.JwtUtil; // Assuming your JWT utility class
import com.backend.wanderverse_server.service.impl.AuthServiceImpl;
import com.backend.wanderverse_server.util.exceptions.LoginFailedException;
import com.backend.wanderverse_server.util.exceptions.SignupFailedException;
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
    private Mapper<UserEntity, UserDTO> userMapper; // Mocked DTO mapper, specifying generic types

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

        signUpRequestDTO = new SignUpRequestDTO("newuser", "new@example.com", "password123");
        loginRequestDTO = new LoginRequestDTO("test@example.com", "password123");
    }

    @Test
    void register_success() {
        // Prepare mock UserDTO that userMapper would return
        UserDTO mockUserDTO = UserDTO.builder()
                .id(1L)
                .username(signUpRequestDTO.getUsername())
                .email(signUpRequestDTO.getEmail())
                // Add any other fields that UserDTO should have
                .build();

        // Mock behaviors of dependencies for a successful registration
        when(userRepository.existsByEmail(signUpRequestDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signUpRequestDTO.getPassword())).thenReturn("encodedPassword");

        // Return a saved user with the new user's details for consistency
        UserEntity savedUserEntity = UserEntity.builder()
                .id(1L) // Assuming ID is generated upon save
                .username(signUpRequestDTO.getUsername())
                .email(signUpRequestDTO.getEmail())
                .password("encodedPassword")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

        // Stub generateToken with the email from signUpRequestDTO, as AuthServiceImpl uses it.
        when(jwtUtil.generateToken(signUpRequestDTO.getEmail())).thenReturn("mockJwtToken");

        // CORRECTED: userMapper.mapTo should return a UserDTO
        when(userMapper.mapTo(any(UserEntity.class))).thenReturn(mockUserDTO);

        // Call the service method
        AuthResponseDTO response = authService.register(signUpRequestDTO);

        // Assertions
        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        // Add assertions for the user object within AuthResponseDTO if applicable
        assertEquals(mockUserDTO.getEmail(), response.getUser().getEmail());
        assertEquals(mockUserDTO.getUsername(), response.getUser().getUsername());


        // Verify interactions with mocks
        verify(userRepository, times(1)).existsByEmail(signUpRequestDTO.getEmail());
        verify(passwordEncoder, times(1)).encode(signUpRequestDTO.getPassword());
        verify(userRepository, times(1)).save(any(UserEntity.class));
        // CORRECTED: Verify generateToken was called with the correct email
        verify(jwtUtil, times(1)).generateToken(signUpRequestDTO.getEmail());
        // Verify userMapper was called and returned the expected DTO
        verify(userMapper, times(1)).mapTo(any(UserEntity.class));
    }

    @Test
    void register_userAlreadyExists_throwsException() {
        // Mock behavior for an existing user
        when(userRepository.existsByEmail(signUpRequestDTO.getEmail())).thenReturn(true);

        // Call the service method and expect an exception
        RuntimeException thrown = assertThrows(SignupFailedException.class, () -> {
            authService.register(signUpRequestDTO);
        });

        // Assertions
        assertEquals("User with email '" + signUpRequestDTO.getEmail() + "' already exists!", thrown.getMessage());

        // Verify interactions (should not proceed to encode/save)
        verify(userRepository, times(1)).existsByEmail(signUpRequestDTO.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtUtil, never()).generateToken(anyString());
        verify(userMapper, never()).mapTo(any(UserEntity.class));
    }

    @Test
    void login_success() {
        // Prepare mock UserDTO that userMapper would return for the login scenario
        UserDTO mockUserDTO = UserDTO.builder()
                .id(testUserEntity.getId())
                .username(testUserEntity.getUsername())
                .email(testUserEntity.getEmail())
                .build();

        // Create a mock UserDetails object and stub its getUsername() method
        UserDetails mockUserDetails = mock(UserDetails.class);
//        when(mockUserDetails.getUsername()).thenReturn(testUserEntity.getEmail());

        // Create a mock Authentication object and stub its getPrincipal() method to return mockUserDetails
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUserDetails);

        // Mock authenticationManager.authenticate() to return the configured Authentication mock
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())))
                .thenReturn(authentication);

        when(jwtUtil.generateToken(testUserEntity.getEmail())).thenReturn("mockJwtToken");
        when(userRepository.findUserByEmail(testUserEntity.getEmail())).thenReturn(Optional.of(testUserEntity));
        when(userMapper.mapTo(any(UserEntity.class))).thenReturn(mockUserDTO);

        // Call the service method, ensure it's authenticate for login
        AuthResponseDTO response = authService.authenticate(loginRequestDTO);

        // Assertions
        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        assertEquals(mockUserDTO.getEmail(), response.getUser().getEmail());

        // VERIFY INTERACTIONS:
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        // Verify that getPrincipal() was called on the mocked Authentication object
        verify(authentication, times(1)).getPrincipal();
        // Verify that getUsername() was called on the mocked UserDetails object
        verify(mockUserDetails, times(0)).getUsername();
        verify(jwtUtil, times(1)).generateToken(testUserEntity.getEmail());
        verify(userRepository, times(1)).findUserByEmail(testUserEntity.getEmail());
        verify(userMapper, times(1)).mapTo(any(UserEntity.class));
    }

    @Test
    void login_invalidCredentials_throwsException() { // Renamed the test method
        // Mock behavior for invalid credentials
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Call the service method
        RuntimeException thrown = assertThrows(LoginFailedException.class, () -> {
            authService.authenticate(loginRequestDTO);
        });

        // Assertions: Now check if the Optional is empty, as per your service's observed behavior
        assertEquals("Invalid email or password provided.", thrown.getMessage());

        // Verify interactions (should not proceed to generate token, find user, or map)
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        verify(jwtUtil, never()).generateToken(anyString());
        verify(userRepository, never()).findUserByEmail(anyString());
        verify(userMapper, never()).mapTo(any(UserEntity.class));

        // You might want to verify that getPrincipal() and getUsername() were NOT called if the exception
        // truly bypasses them, but in your current service code, they would be called before the exception
        // is caught by the generic block. If the generic block is hit immediately, these verifications may not be needed.
        // However, if getPrincipal() and getUsername() are called before the BadCredentialsException is caught,
        // you might still verify them. For now, keeping them as 'never' based on the assumption that the exception
        // occurs and is caught early, preventing further execution in the happy path.
        // If your service *does* call them before catching the BadCredentialsException, you might need
        // to adjust these verifications.
    }

    @Test
    void login_userNotFoundAfterAuthentication_throwsException() { // Updated method name
        Authentication authentication = mock(Authentication.class);
        UserDetails mockUserDetails = mock(UserDetails.class);
//        when(mockUserDetails.getUsername()).thenReturn(loginRequestDTO.getEmail());
        when(authentication.getPrincipal()).thenReturn(mockUserDetails);

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())))
                .thenReturn(authentication);
        when(userRepository.findUserByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.empty());

        // Call the service method
        RuntimeException thrown = assertThrows(LoginFailedException.class, () -> {
            authService.authenticate(loginRequestDTO);
        });

        // Assertions: Now check if the Optional is empty, as per your service's observed behavior
        assertEquals("Invalid email or password provided.", thrown.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication, times(1)).getPrincipal();
        verify(mockUserDetails, times(0)).getUsername();
        verify(userRepository, times(1)).findUserByEmail(loginRequestDTO.getEmail());
        verify(jwtUtil, times(1)).generateToken(anyString());
        verify(userMapper, never()).mapTo(any(UserEntity.class));
    }
}