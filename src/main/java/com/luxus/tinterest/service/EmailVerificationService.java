package com.luxus.tinterest.service;

import com.luxus.tinterest.entity.EmailVerification;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.verify.EmailAlreadyVerifiedException;
import com.luxus.tinterest.exception.verify.InvalidVerificationCodeException;
import com.luxus.tinterest.exception.verify.TooManyAttemptsException;
import com.luxus.tinterest.exception.verify.VerificationCodeExpiredException;
import com.luxus.tinterest.repository.EmailVerificationRepository;
import com.luxus.tinterest.repository.UserRepository;
import com.luxus.tinterest.util.CodeHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int CODE_TTL_SECONDS = 900;      // 15 минут
    private static final int MAX_ATTEMPTS = 5;

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    private final CodeHasher codeHasher;

    @Transactional
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

    @Transactional
    public void verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidVerificationCodeException::new);

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        EmailVerification verification = emailVerificationRepository
                .findByUser(user)
                .orElseThrow(InvalidVerificationCodeException::new);

        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new VerificationCodeExpiredException();
        }

        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            throw new TooManyAttemptsException();
        }

        verification.setAttempts(verification.getAttempts() + 1);
        emailVerificationRepository.save(verification);

        if (!codeHasher.matches(code, verification.getCodeHash())) {
            throw new InvalidVerificationCodeException();
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationRepository.delete(verification);
    }


    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }
}
