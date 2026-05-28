package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.email.EmailResendRequestDto;
import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.exception.handler.EmailVerificationHandler;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.verify.EmailAlreadyVerifiedException;
import com.luxus.tinterest.exception.verify.InvalidVerificationCodeException;
import com.luxus.tinterest.exception.verify.TooManyAttemptsException;
import com.luxus.tinterest.exception.verify.VerificationCodeExpiredException;
import com.luxus.tinterest.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Verification Controller Tests")
class EmailVerificationControllerTests {

    @Mock
    private AuthService authService;

    @InjectMocks
    private EmailVerificationController emailVerificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private EmailVerifyRequestDto validVerifyRequest;
    private EmailResendRequestDto validResendRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(emailVerificationController)
                .setControllerAdvice(new EmailVerificationHandler(), new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        validVerifyRequest = new EmailVerifyRequestDto("john@example.com", "123456");
        validResendRequest = new EmailResendRequestDto("john@example.com");
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/email/verify - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully verify email with valid code")
    void testSuccessfulEmailVerification() throws Exception {
        doNothing().when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email has been verified"));
    }

    @Test
    @DisplayName("Should allow email verification without authentication")
    void testEmailVerificationWithoutAuthentication() throws Exception {
        doNothing().when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email has been verified"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/email/verify - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when email is missing")
    void testEmailVerifyWithoutEmail() throws Exception {
        EmailVerifyRequestDto request = new EmailVerifyRequestDto(null, "123456");

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when verification code is missing")
    void testEmailVerifyWithoutCode() throws Exception {
        EmailVerifyRequestDto request = new EmailVerifyRequestDto("john@example.com", null);

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when verification code is invalid")
    void testEmailVerifyWithInvalidCode() throws Exception {
        doThrow(new InvalidVerificationCodeException()).when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when verification code is expired")
    void testEmailVerifyWithExpiredCode() throws Exception {
        doThrow(new VerificationCodeExpiredException()).when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email is already verified")
    void testEmailVerifyAlreadyVerified() throws Exception {
        doThrow(new EmailAlreadyVerifiedException()).when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 429 when too many verification attempts")
    void testEmailVerifyTooManyAttempts() throws Exception {
        EmailVerifyRequestDto request = new EmailVerifyRequestDto("john@example.com", "123456");
        doNothing().when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/v1/auth/email/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        doThrow(new TooManyAttemptsException()).when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testEmailVerifyUserNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/email/resend - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully resend verification code")
    void testSuccessfulEmailResend() throws Exception {
        doNothing().when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code has been resent"));
    }

    @Test
    @DisplayName("Should allow email resend without authentication")
    void testEmailResendWithoutAuthentication() throws Exception {
        doNothing().when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code has been resent"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/email/resend - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when email is missing on resend")
    void testEmailResendWithoutEmail() throws Exception {
        EmailResendRequestDto request = new EmailResendRequestDto(null);

        mockMvc.perform(post("/v1/auth/email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user not found on resend")
    void testEmailResendUserNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when email is already verified on resend")
    void testEmailResendAlreadyVerified() throws Exception {
        doThrow(new EmailAlreadyVerifiedException()).when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 429 when too many resend attempts")
    void testEmailResendTooManyAttempts() throws Exception {
        EmailResendRequestDto request = new EmailResendRequestDto("john@example.com");
        doNothing().when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/v1/auth/email/resend")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        doThrow(new TooManyAttemptsException()).when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isBadRequest());
    }
}
