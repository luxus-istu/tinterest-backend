package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.email.EmailResendRequestDto;
import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.handler.EmailVerificationHandler;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Скорее всего не выполнятся из-за:
// Коды ошибок не совпадают с теми, что выбрасывает, хз как надо, исправьте как вам надо
// - QA

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
                .setControllerAdvice(new GlobalExceptionHandler(), new EmailVerificationHandler())
                .build();

        objectMapper = new ObjectMapper();

        validVerifyRequest = new EmailVerifyRequestDto("john@example.com", "123456");
        validResendRequest = new EmailResendRequestDto("john@example.com");
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/email/verify
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully verify email with valid code")
    void testSuccessfulEmailVerify() throws Exception {
        doNothing().when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email has been verified"));
    }

    @Test
    @DisplayName("Should return 400 when email is missing on verify")
    void testVerifyWithoutEmail() throws Exception {
        validVerifyRequest.setEmail(null);

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid on verify")
    void testVerifyWithInvalidEmail() throws Exception {
        validVerifyRequest.setEmail("not-an-email");

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when code is missing on verify")
    void testVerifyWithoutCode() throws Exception {
        validVerifyRequest.setCode(null);

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when code is blank on verify")
    void testVerifyWithBlankCode() throws Exception {
        validVerifyRequest.setCode("   ");

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is empty on verify")
    void testVerifyWithEmptyBody() throws Exception {
        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when verification code is invalid")
    void testVerifyWithInvalidCode() throws Exception {
        doThrow(new InvalidVerificationCodeException())
                .when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when email is already verified")
    void testVerifyWithAlreadyVerifiedEmail() throws Exception {
        doThrow(new EmailAlreadyVerifiedException())
                .when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 when verification code is expired")
    void testVerifyWithExpiredCode() throws Exception {
        doThrow(new VerificationCodeExpiredException())
                .when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when too many verification attempts")
    void testVerifyWithTooManyAttempts() throws Exception {
        doThrow(new TooManyAttemptsException())
                .when(authService).verifyEmailCode(any(EmailVerifyRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVerifyRequest)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/email/resend
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
    @DisplayName("Should return 400 when email is missing on resend")
    void testResendWithoutEmail() throws Exception {
        validResendRequest.setEmail(null);

        mockMvc.perform(post("/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid on resend")
    void testResendWithInvalidEmail() throws Exception {
        validResendRequest.setEmail("not-an-email");

        mockMvc.perform(post("/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when request body is empty on resend")
    void testResendWithEmptyBody() throws Exception {
        mockMvc.perform(post("/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user is not found on resend")
    void testResendWithNonExistentEmail() throws Exception {
        doThrow(new UserNotFoundException())
                .when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 409 when email is already verified on resend")
    void testResendWithAlreadyVerifiedEmail() throws Exception {
        doThrow(new EmailAlreadyVerifiedException())
                .when(authService).resendEmailCode(any(EmailResendRequestDto.class));

        mockMvc.perform(post("/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResendRequest)))
                .andExpect(status().isConflict());
    }
}