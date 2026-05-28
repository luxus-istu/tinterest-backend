package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.email.EmailResendRequestDto;
import com.luxus.tinterest.dto.email.EmailResendResponseDto;
import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.dto.email.EmailVerifyResponseDto;
import com.luxus.tinterest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final AuthService authService;

    @PostMapping("/auth/email/verify")
    public ResponseEntity<EmailVerifyResponseDto> emailVerify(@Valid @RequestBody EmailVerifyRequestDto emailVerifyDto) {
        log.info("Email verification attempt for email: {}", emailVerifyDto.getEmail());
        authService.verifyEmailCode(emailVerifyDto);
        log.info("Email successfully verified for: {}", emailVerifyDto.getEmail());
        return ResponseEntity.ok(new EmailVerifyResponseDto("Email has been verified"));
    }

    @PostMapping("/auth/email/resend")
    public ResponseEntity<EmailResendResponseDto> emailResend(@Valid @RequestBody EmailResendRequestDto emailResendRequestDto) {
        log.info("Request to resend verification code for: {}", emailResendRequestDto.getEmail());
        authService.resendEmailCode(emailResendRequestDto);
        log.info("Verification code resent for: {}", emailResendRequestDto.getEmail());
        return ResponseEntity.ok(new EmailResendResponseDto("Verification code has been resent"));
    }
}
