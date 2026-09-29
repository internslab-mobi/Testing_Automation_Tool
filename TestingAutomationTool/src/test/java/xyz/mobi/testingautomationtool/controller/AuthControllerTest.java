package xyz.mobi.testingautomationtool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.mobi.testingautomationtool.dto.request.auth.LoginRequest;
import xyz.mobi.testingautomationtool.dto.request.auth.RegisterRequest;
import xyz.mobi.testingautomationtool.dto.response.auth.AuthResponse;
import xyz.mobi.testingautomationtool.dto.response.auth.UserProfileResponse;
import xyz.mobi.testingautomationtool.service.AuthService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegisterEndpoint() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("manager_user")
                .email("manager@example.com")
                .password("password123")
                .fullName("Manager User")
                .role("MANAGER")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt.token.value")
                .type("Bearer")
                .userId(1)
                .username("manager_user")
                .email("manager@example.com")
                .role("MANAGER")
                .fullName("Manager User")
                .message("User registered successfully")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.token.value"))
                .andExpect(jsonPath("$.username").value("manager_user"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void testLoginEndpoint() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("tester_user")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt.token.value")
                .type("Bearer")
                .userId(2)
                .username("tester_user")
                .email("tester@example.com")
                .role("TESTER")
                .fullName("Tester User")
                .message("User logged in successfully")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.value"))
                .andExpect(jsonPath("$.username").value("tester_user"))
                .andExpect(jsonPath("$.role").value("TESTER"));
    }
}
