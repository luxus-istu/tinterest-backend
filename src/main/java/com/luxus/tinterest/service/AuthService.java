package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final EmailService emailService;

    public void register(RegistrationRequestDto requestDto) {
        String code = userService.register(requestDto);
        emailService.sendCode(requestDto.getEmail(), code);
    }
}
