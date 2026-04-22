package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.email.EmailVerifyRequestDto;
import com.luxus.tinterest.dto.email.EmailVerifyResponseDto;
import com.luxus.tinterest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final AuthService authService;

    @PostMapping("/auth/email/verify")
    public ResponseEntity<EmailVerifyResponseDto> emailVerify(@Valid @RequestBody EmailVerifyRequestDto emailVerifyDto) {
        authService.verifyEmailCode(emailVerifyDto);
        return ResponseEntity.ok(new EmailVerifyResponseDto("Email подтверждён"));
    }

}
