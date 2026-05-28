package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.dto.registration.RegistrationResponseDto;
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
public class RegistrationController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<RegistrationResponseDto> register(@Valid @RequestBody RegistrationRequestDto requestDto) {
        log.info("Registration attempt for email: {}", requestDto.getEmail());
        authService.register(requestDto);
        log.info("Registration successful for email: {}. Verification code sent.", requestDto.getEmail());
        return ResponseEntity.ok(new RegistrationResponseDto(requestDto.getEmail(), "Verification code has been sent"));
    }
}
