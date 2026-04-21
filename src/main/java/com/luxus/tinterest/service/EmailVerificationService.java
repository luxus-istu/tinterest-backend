package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.EmailVerification;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.repository.EmailVerificationRepository;
import com.luxus.tinterest.util.CodeHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_SECONDS = 900;      // 15 минут
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final EmailVerificationRepository emailVerificationRepository;
    private final CodeHasher codeHasher;

    public String generateAndSave(User user) {
        String code = generateCode();

        EmailVerification verification = emailVerificationRepository
                .findByUser(user)
                .orElse(EmailVerification.builder().user(user).build());

        verification.setCodeHash(codeHasher.hash(code));
        verification.setCreatedAt(Instant.now());
        verification.setExpiresAt(Instant.now().plusSeconds(CODE_TTL_SECONDS));
        verification.setAttempts(0);

        emailVerificationRepository.save(verification);

        return code;
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }
}
