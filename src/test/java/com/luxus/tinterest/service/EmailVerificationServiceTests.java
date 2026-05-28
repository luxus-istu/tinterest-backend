package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.EmailVerification;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.verify.EmailAlreadyVerifiedException;
import com.luxus.tinterest.exception.verify.InvalidVerificationCodeException;
import com.luxus.tinterest.exception.verify.TooManyAttemptsException;
import com.luxus.tinterest.exception.verify.VerificationCodeExpiredException;
import com.luxus.tinterest.repository.EmailVerificationRepository;
import com.luxus.tinterest.repository.UserRepository;
import com.luxus.tinterest.util.Sha256Hasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService Unit Tests")
class EmailVerificationServiceTests {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Sha256Hasher sha256Hasher;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
    }

    @Test
    @DisplayName("Should generate and save verification code for new user")
    void testGenerateAndSaveCreatesVerification() {
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.empty());
        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));

        String code = emailVerificationService.generateAndSave(user);

        assertNotNull(code);
        assertEquals(6, code.length());

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationRepository).save(captor.capture());

        EmailVerification saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals("hash-" + code, saved.getCodeHash());
        assertEquals(0, saved.getAttempts());
        assertTrue(saved.getExpiresAt().isAfter(saved.getCreatedAt()));
    }

    @Test
    @DisplayName("Should verify code and delete verification record")
    void testVerifyCodeSuccess() {
        user.setEmailVerified(false);
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .codeHash("hash-123456")
                .expiresAt(Instant.now().plusSeconds(60))
                .attempts(0)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));
        when(sha256Hasher.matches("123456", "hash-123456")).thenReturn(true);

        emailVerificationService.verifyCode("user@example.com", "123456");

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
        verify(emailVerificationRepository).delete(verification);
    }

    @Test
    @DisplayName("Should throw when email already verified")
    void testVerifyCodeThrowsWhenAlreadyVerified() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyVerifiedException.class,
                () -> emailVerificationService.verifyCode("user@example.com", "123456"));
    }

    @Test
    @DisplayName("Should throw when verification record is missing")
    void testVerifyCodeThrowsWhenNoVerificationFound() {
        user.setEmailVerified(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(InvalidVerificationCodeException.class,
                () -> emailVerificationService.verifyCode("user@example.com", "123456"));
    }

    @Test
    @DisplayName("Should throw when verification code expired")
    void testVerifyCodeThrowsWhenExpired() {
        user.setEmailVerified(false);
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .codeHash("hash-123456")
                .expiresAt(Instant.now().minusSeconds(1))
                .attempts(0)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));

        assertThrows(VerificationCodeExpiredException.class,
                () -> emailVerificationService.verifyCode("user@example.com", "123456"));
    }

    @Test
    @DisplayName("Should throw when verification attempts exceeded")
    void testVerifyCodeThrowsWhenTooManyAttempts() {
        user.setEmailVerified(false);
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .codeHash("hash-123456")
                .expiresAt(Instant.now().plusSeconds(60))
                .attempts(5)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));

        assertThrows(TooManyAttemptsException.class,
                () -> emailVerificationService.verifyCode("user@example.com", "123456"));
    }

    @Test
    @DisplayName("Should throw when verification code is invalid")
    void testVerifyCodeThrowsWhenInvalidCode() {
        user.setEmailVerified(false);
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .codeHash("hash-123456")
                .expiresAt(Instant.now().plusSeconds(60))
                .attempts(0)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));
        when(sha256Hasher.matches("123456", "hash-123456")).thenReturn(false);

        assertThrows(InvalidVerificationCodeException.class,
                () -> emailVerificationService.verifyCode("user@example.com", "123456"));
        assertEquals(1, verification.getAttempts());
    }

    @Test
    @DisplayName("Should resend verification code when the user exists and is not verified")
    void testResendCodeReturnsCode() {
        user.setEmailVerified(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.empty());
        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));

        String code = emailVerificationService.resendCode("user@example.com");

        assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test
    @DisplayName("Should throw when resend code for missing user")
    void testResendCodeThrowsWhenUserMissing() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> emailVerificationService.resendCode("user@example.com"));
    }

    @Test
    @DisplayName("Should throw when resend code for already verified user")
    void testResendCodeThrowsWhenAlreadyVerified() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyVerifiedException.class,
                () -> emailVerificationService.resendCode("user@example.com"));
    }
}
