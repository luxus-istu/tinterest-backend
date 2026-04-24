package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.RefreshToken;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.login.InvalidRefreshTokenException;
import com.luxus.tinterest.exception.login.RefreshTokenExpiredException;
import com.luxus.tinterest.exception.login.RefreshTokenReusedException;
import com.luxus.tinterest.exception.login.RefreshTokenRevokedException;
import com.luxus.tinterest.repository.RefreshTokenRepository;
import com.luxus.tinterest.util.CodeHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final CodeHasher codeHasher;

    @Value("${app.jwt.refresh-token-expiration-days:15}")
    private int expirationDays;

    @Transactional
    public String generateAndSave(User user) {
        UUID familyId = UUID.randomUUID();

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = codeHasher.hash(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(tokenHash)
                .familyId(familyId)
                .expiresAt(LocalDateTime.now().plusDays(expirationDays))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional(noRollbackFor = RefreshTokenReusedException.class)
    public RotateResult rotate(String rawToken) {
        String tokenHash = codeHasher.hash(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (existing.isUsed()) {
            refreshTokenRepository.revokeAllByFamilyId(existing.getFamilyId());
            throw new RefreshTokenReusedException();
        }

        if (existing.isRevoked()) {
            throw new RefreshTokenRevokedException();
        }

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException();
        }

        existing.setUsed(true);
        refreshTokenRepository.save(existing);

        String newRawToken = UUID.randomUUID().toString();
        String newTokenHash = codeHasher.hash(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(existing.getUserId())
                .tokenHash(newTokenHash)
                .familyId(existing.getFamilyId())
                .expiresAt(LocalDateTime.now().plusDays(expirationDays))
                .build();

        refreshTokenRepository.save(newToken);

        return new RotateResult(newRawToken, newToken.getUserId());
    }

    public record RotateResult(String rawToken, Long userId) {}
}
