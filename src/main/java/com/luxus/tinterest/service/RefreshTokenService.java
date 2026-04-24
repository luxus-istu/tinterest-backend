package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.RefreshToken;
import com.luxus.tinterest.entity.User;
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

}
