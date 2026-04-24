package com.luxus.tinterest.controller;


import com.luxus.tinterest.dto.login.LoginRequestDto;
import com.luxus.tinterest.dto.login.LoginResponseDto;
import com.luxus.tinterest.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        Map<String, String> tokens = authService.login(requestDto);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", tokens.get("refreshToken"))
                .httpOnly(true)
                .secure(true)
                .path("/v1/auth")
                .maxAge(Duration.ofDays(15))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        return ResponseEntity.ok(new LoginResponseDto(tokens.get("accessToken")));
    }

}
