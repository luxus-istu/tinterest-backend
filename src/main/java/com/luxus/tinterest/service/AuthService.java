package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.email.EmailResendRequestDto;
import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.dto.login.LoginRequestDto;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;


    public void register(RegistrationRequestDto requestDto) {
        String code = userService.register(requestDto);
        emailService.sendCode(requestDto.getEmail(), code);
    }

    public void verifyEmailCode(EmailVerifyRequestDto emailVerifyDto) {
        emailVerificationService.verifyCode(emailVerifyDto.getEmail(), emailVerifyDto.getCode());
    }

    public void resendEmailCode(EmailResendRequestDto emailResendRequestDto) {
        String code = emailVerificationService.resendCode(emailResendRequestDto.getEmail());
        emailService.sendCode(emailResendRequestDto.getEmail(), code);
    }

    public Map<String, String> login(LoginRequestDto loginRequestDto) {
        User user = userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = refreshTokenService.generateAndSave(user);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public Map<String, String> refresh(String refreshToken) {
        RefreshTokenService.RotateResult result = refreshTokenService.rotate(refreshToken);

        User user = userService.findById(result.userId());

        String accessToken = jwtService.generateAccessToken(user);

        return Map.of("accessToken", accessToken, "refreshToken", result.rawToken());
    }
}
