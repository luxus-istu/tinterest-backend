package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.login.LoginRequestDto;
import com.luxus.tinterest.exception.handler.LoginHandler;
import com.luxus.tinterest.exception.login.InvalidCredentialsException;
import com.luxus.tinterest.service.AuthService;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Login Controller Tests")
class LoginControllerTests {

    @Mock
    private AuthService authService;

    @InjectMocks
    private LoginController loginController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequestDto validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .setControllerAdvice(new LoginHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        validRequest = new LoginRequestDto();
        validRequest.setEmail("john@example.com");
        validRequest.setPassword("securePassword123");
    }

    private void mockAuthenticationPrincipal(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/login - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testSuccessfulLogin() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(Map.of("accessToken", "access.jwt.token", "refreshToken", "refresh.jwt.token"));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.jwt.token"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("Should allow login without prior authentication")
    void testLoginWithoutAuthentication() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(Map.of("accessToken", "access.jwt.token", "refreshToken", "refresh.jwt.token"));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.jwt.token"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/login - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when email is missing")
    void testLoginWithoutEmail() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setPassword("password");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is missing")
    void testLoginWithoutPassword() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void testLoginWithInvalidCredentials() throws Exception {
        doThrow(new InvalidCredentialsException()).when(authService).login(any(LoginRequestDto.class));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/refresh - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully refresh token with valid refresh token")
    void testSuccessfulRefresh() throws Exception {
        mockAuthenticationPrincipal(1L);

        when(authService.refresh("valid.refresh.token"))
                .thenReturn(Map.of("accessToken", "new.access.token", "refreshToken", "new.refresh.token"));

        mockMvc.perform(post("/v1/auth/refresh").cookie(new Cookie("refresh_token", "valid.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/refresh - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 401 when refresh token is invalid")
    void testRefreshWithInvalidToken() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh")
                .header("Cookie", "refresh_token=invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when refresh token is missing")
    void testRefreshWithoutToken() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/logout - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully logout and clear refresh token")
    void testSuccessfulLogout() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                .header("Cookie", "refresh_token=valid.token"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/logout - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 204 even when logout without refresh token")
    void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }
}
