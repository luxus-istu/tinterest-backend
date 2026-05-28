package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.email.EmailResendRequestDto;
import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.dto.login.LoginRequestDto;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.login.InvalidCredentialsException;
import com.luxus.tinterest.exception.login.InvalidRefreshTokenException;
import com.luxus.tinterest.exception.registration.UserAlreadyRegisteredException;
import com.luxus.tinterest.exception.verify.InvalidVerificationCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegistrationRequestDto registrationRequest;
    private EmailVerifyRequestDto emailVerifyRequest;
    private EmailResendRequestDto emailResendRequest;
    private LoginRequestDto loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequestDto();
        registrationRequest.setEmail("user@example.com");
        registrationRequest.setPassword("password123");

        emailVerifyRequest = new EmailVerifyRequestDto();
        emailVerifyRequest.setEmail("user@example.com");
        emailVerifyRequest.setCode("123456");

        emailResendRequest = new EmailResendRequestDto();
        emailResendRequest.setEmail("user@example.com");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
    }

    @Test
    @DisplayName("Should register user and send verification email")
    void testRegisterSendsVerificationEmail() {
        when(userService.register(any(RegistrationRequestDto.class))).thenReturn("code-abc");
        doNothing().when(emailService).sendCode(anyString(), anyString());

        authService.register(registrationRequest);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(userService).register(registrationRequest);
        verify(emailService).sendCode(emailCaptor.capture(), codeCaptor.capture());

        assertEquals("user@example.com", emailCaptor.getValue());
        assertEquals("code-abc", codeCaptor.getValue());
    }

    @Test
    @DisplayName("Should throw when user is already registered")
    void testRegisterThrowsWhenUserAlreadyRegistered() {
        when(userService.register(any(RegistrationRequestDto.class)))
                .thenThrow(new UserAlreadyRegisteredException());

        assertThrows(UserAlreadyRegisteredException.class,
                () -> authService.register(registrationRequest));
        verify(userService).register(registrationRequest);
    }

    @Test
    @DisplayName("Should verify email code through emailVerificationService")
    void testVerifyEmailCode() {
        doNothing().when(emailVerificationService).verifyCode(anyString(), anyString());

        authService.verifyEmailCode(emailVerifyRequest);

        verify(emailVerificationService).verifyCode("user@example.com", "123456");
    }

    @Test
    @DisplayName("Should propagate invalid verification code exception")
    void testVerifyEmailCodeThrowsWhenInvalid() {
        doThrow(new InvalidVerificationCodeException()).when(emailVerificationService)
                .verifyCode(anyString(), anyString());

        assertThrows(InvalidVerificationCodeException.class,
                () -> authService.verifyEmailCode(emailVerifyRequest));
        verify(emailVerificationService).verifyCode("user@example.com", "123456");
    }

    @Test
    @DisplayName("Should resend email code and send it by emailService")
    void testResendEmailCode() {
        when(emailVerificationService.resendCode("user@example.com")).thenReturn("resent-code");

        authService.resendEmailCode(emailResendRequest);

        verify(emailVerificationService).resendCode("user@example.com");
        verify(emailService).sendCode("user@example.com", "resent-code");
    }

    @Test
    @DisplayName("Should throw when resendCode fails")
    void testResendEmailCodeThrowsWhenImpossible() {
        when(emailVerificationService.resendCode("user@example.com"))
                .thenThrow(new InvalidVerificationCodeException());

        assertThrows(InvalidVerificationCodeException.class,
                () -> authService.resendEmailCode(emailResendRequest));
        verify(emailVerificationService).resendCode("user@example.com");
    }

    @Test
    @DisplayName("Should login user and return access and refresh tokens")
    void testLoginReturnsTokens() {
        when(userService.login("user@example.com", "password123")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.generateAndSave(user)).thenReturn("refresh-token");

        Map<String, String> result = authService.login(loginRequest);

        assertEquals("access-token", result.get("accessToken"));
        assertEquals("refresh-token", result.get("refreshToken"));
        verify(userService).login("user@example.com", "password123");
        verify(jwtService).generateAccessToken(user);
        verify(refreshTokenService).generateAndSave(user);
    }

    @Test
    @DisplayName("Should throw invalid credentials on login failure")
    void testLoginThrowsInvalidCredentials() {
        when(userService.login("user@example.com", "password123"))
                .thenThrow(new InvalidCredentialsException());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));
        verify(userService).login("user@example.com", "password123");
    }

    @Test
    @DisplayName("Should refresh tokens using rotated refresh token")
    void testRefreshReturnsNewTokens() {
        RefreshTokenService.RotateResult rotateResult = new RefreshTokenService.RotateResult("new.refresh.token", 1L);
        when(refreshTokenService.rotate("old.refresh.token")).thenReturn(rotateResult);
        when(userService.findById(1L)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        Map<String, String> result = authService.refresh("old.refresh.token");

        assertEquals("new-access-token", result.get("accessToken"));
        assertEquals("new.refresh.token", result.get("refreshToken"));
        verify(refreshTokenService).rotate("old.refresh.token");
        verify(userService).findById(1L);
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    @DisplayName("Should throw invalid refresh token on refresh failure")
    void testRefreshThrowsInvalidRefreshToken() {
        when(refreshTokenService.rotate("old.refresh.token"))
                .thenThrow(new InvalidRefreshTokenException());

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh("old.refresh.token"));
        verify(refreshTokenService).rotate("old.refresh.token");
    }

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void testLogoutRevokesRefreshToken() {
        doNothing().when(refreshTokenService).revoke("refresh-token-to-revoke");

        authService.logout("refresh-token-to-revoke");

        verify(refreshTokenService).revoke("refresh-token-to-revoke");
    }

    @Test
    @DisplayName("Should throw invalid refresh token on logout failure")
    void testLogoutThrowsInvalidRefreshToken() {
        doThrow(new InvalidRefreshTokenException()).when(refreshTokenService)
                .revoke("invalid.refresh-token");

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.logout("invalid.refresh-token"));
        verify(refreshTokenService).revoke("invalid.refresh-token");
    }
}
