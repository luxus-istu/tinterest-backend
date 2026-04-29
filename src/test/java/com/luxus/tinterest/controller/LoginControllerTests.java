package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.login.LoginRequestDto;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.handler.LoginHandler;
import com.luxus.tinterest.exception.login.InvalidRefreshTokenException;
import com.luxus.tinterest.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .build();
        objectMapper = new ObjectMapper();

        validRequest = new LoginRequestDto();
        validRequest.setEmail("john@example.com");
        validRequest.setPassword("securePassword123");
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully login and return access token")
    void testSuccessfulLogin() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(Map.of("accessToken", "access.jwt.token", "refreshToken", "refresh.jwt.token"));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.jwt.token"));
    }

    @Test
    @DisplayName("Should set HttpOnly refresh token cookie on login")
    void testLoginSetsRefreshCookie() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(Map.of("accessToken", "access.jwt.token", "refreshToken", "refresh.jwt.token"));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh.jwt.token")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")));
    }

    @Test
    @DisplayName("Should return 400 when email is missing on login")
    void testLoginWithoutEmail() throws Exception {
        validRequest.setEmail(null);

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid on login")
    void testLoginWithInvalidEmail() throws Exception {
        validRequest.setEmail("not-an-email");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is missing on login")
    void testLoginWithoutPassword() throws Exception {
        validRequest.setPassword(null);

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is empty on login")
    void testLoginWithEmptyBody() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/refresh
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully refresh tokens and return new access token")
    void testSuccessfulRefresh() throws Exception {
        when(authService.refresh("old.refresh.token"))
                .thenReturn(Map.of("accessToken", "new.access.token", "refreshToken", "new.refresh.token"));

        mockMvc.perform(post("/v1/auth/refresh")
                .cookie(new Cookie("refresh_token", "old.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"));
    }

    @Test
    @DisplayName("Should set new refresh token cookie after refresh")
    void testRefreshSetsNewCookie() throws Exception {
        when(authService.refresh("old.refresh.token"))
                .thenReturn(Map.of("accessToken", "new.access.token", "refreshToken", "new.refresh.token"));

        mockMvc.perform(post("/v1/auth/refresh")
                .cookie(new Cookie("refresh_token", "old.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=new.refresh.token")));
    }

    @Test
    @DisplayName("Should return 401 when refresh token cookie is missing")
    void testRefreshWithoutCookie() throws Exception {
        mockMvc.perform(post("/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when refresh token is invalid")
    void testRefreshWithInvalidToken() throws Exception {
        when(authService.refresh("invalid.token"))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/v1/auth/refresh")
                .cookie(new Cookie("refresh_token", "invalid.token")))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/logout
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully logout and return 204")
    void testSuccessfulLogout() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                .cookie(new Cookie("refresh_token", "some.refresh.token")))
                .andExpect(status().isNoContent());

        verify(authService).logout("some.refresh.token");
    }

    @Test
    @DisplayName("Should delete refresh token cookie on logout")
    void testLogoutDeletesRefreshCookie() throws Exception {
        mockMvc.perform(post("/v1/auth/logout")
                .cookie(new Cookie("refresh_token", "some.refresh.token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("Should return 204 even when refresh token cookie is absent on logout")
    void testLogoutWithoutCookie() throws Exception {
        mockMvc.perform(post("/v1/auth/logout"))
                .andExpect(status().isNoContent());

        verify(authService, never()).logout(anyString());
    }
}