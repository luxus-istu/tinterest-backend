package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;


    public void register(RegistrationRequestDto requestDto) {
        String code = userService.register(requestDto);
        emailService.sendCode(requestDto.getEmail(), code);
    }

    public void verifyEmailCode(EmailVerifyRequestDto emailVerifyDto) {
        emailVerificationService.verifyCode(emailVerifyDto.getEmail(), emailVerifyDto.getCode());
    }
}
