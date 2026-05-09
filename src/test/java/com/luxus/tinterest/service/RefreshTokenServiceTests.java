package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.RefreshToken;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.login.InvalidRefreshTokenException;
import com.luxus.tinterest.exception.login.RefreshTokenExpiredException;
import com.luxus.tinterest.exception.login.RefreshTokenReusedException;
import com.luxus.tinterest.exception.login.RefreshTokenRevokedException;
import com.luxus.tinterest.repository.RefreshTokenRepository;
import com.luxus.tinterest.util.Sha256Hasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTests {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private Sha256Hasher sha256Hasher;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = new User();
        user.setId(1L);

        java.lang.reflect.Field expirationDaysField = RefreshTokenService.class.getDeclaredField("expirationDays");
        expirationDaysField.setAccessible(true);
        expirationDaysField.set(refreshTokenService, 15);
    }

    @Test
    @DisplayName("Should generate and save a new refresh token")
    void testGenerateAndSaveReturnsRawToken() {
        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = refreshTokenService.generateAndSave(user);

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertEquals(user.getId(), saved.getUserId());
        assertTrue(saved.getTokenHash().startsWith("hash-"));
        assertNotNull(saved.getFamilyId());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should rotate refresh token successfully")
    void testRotateCreatesNewToken() {
        String rawToken = "raw-token";
        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));

        RefreshToken existing = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash-" + rawToken)
                .familyId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .used(false)
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash("hash-" + rawToken)).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RotateResult result = refreshTokenService.rotate(rawToken);

        assertNotNull(result);
        assertEquals(user.getId(), result.userId());
        assertNotNull(result.rawToken());
        assertFalse(result.rawToken().isBlank());
        assertTrue(existing.isUsed());
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw invalid refresh token when token is missing")
    void testRotateThrowsWhenTokenMissing() {
        when(sha256Hasher.hash(anyString())).thenReturn("hash-missing");
        when(refreshTokenRepository.findByTokenHash("hash-missing")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.rotate("missing-token"));
    }

    @Test
    @DisplayName("Should throw refresh token reused and revoke family")
    void testRotateThrowsWhenTokenReused() {
        String rawToken = "raw-token";
        RefreshToken existing = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash-" + rawToken)
                .familyId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .used(true)
                .revoked(false)
                .build();

        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));
        when(refreshTokenRepository.findByTokenHash("hash-" + rawToken)).thenReturn(Optional.of(existing));

        assertThrows(RefreshTokenReusedException.class,
                () -> refreshTokenService.rotate(rawToken));
        verify(refreshTokenRepository).revokeAllByFamilyId(existing.getFamilyId());
    }

    @Test
    @DisplayName("Should throw refresh token revoked")
    void testRotateThrowsWhenRevoked() {
        String rawToken = "raw-token";
        RefreshToken existing = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash-" + rawToken)
                .familyId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .used(false)
                .revoked(true)
                .build();

        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));
        when(refreshTokenRepository.findByTokenHash("hash-" + rawToken)).thenReturn(Optional.of(existing));

        assertThrows(RefreshTokenRevokedException.class,
                () -> refreshTokenService.rotate(rawToken));
    }

    @Test
    @DisplayName("Should throw refresh token expired")
    void testRotateThrowsWhenExpired() {
        String rawToken = "raw-token";
        RefreshToken existing = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash-" + rawToken)
                .familyId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .used(false)
                .revoked(false)
                .build();

        when(sha256Hasher.hash(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));
        when(refreshTokenRepository.findByTokenHash("hash-" + rawToken)).thenReturn(Optional.of(existing));

        assertThrows(RefreshTokenExpiredException.class,
                () -> refreshTokenService.rotate(rawToken));
    }

    @Test
    @DisplayName("Should revoke refresh token family")
    void testRevokeRevokesFamily() {
        String rawToken = "raw-token";
        RefreshToken existing = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash("hash-" + rawToken)
                .familyId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .used(false)
                .revoked(false)
                .build();

        when(sha256Hasher.hash(anyString())).thenReturn("hash-" + rawToken);
        when(refreshTokenRepository.findByTokenHash("hash-" + rawToken)).thenReturn(Optional.of(existing));
        doNothing().when(refreshTokenRepository).revokeAllByFamilyId(existing.getFamilyId());

        refreshTokenService.revoke(rawToken);

        verify(refreshTokenRepository).revokeAllByFamilyId(existing.getFamilyId());
    }

    @Test
    @DisplayName("Should throw invalid refresh token when revoke fails")
    void testRevokeThrowsWhenTokenMissing() {
        when(sha256Hasher.hash(anyString())).thenReturn("hash-missing");
        when(refreshTokenRepository.findByTokenHash("hash-missing")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.revoke("missing-token"));
    }
}
