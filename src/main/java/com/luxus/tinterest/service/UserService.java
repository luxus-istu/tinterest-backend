package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.entity.Role;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.UserAlreadyRegisteredException;
import com.luxus.tinterest.mapper.UserMapper;
import com.luxus.tinterest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final UserMapper userMapper;

    @Transactional
    public String register(RegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new UserAlreadyRegisteredException();
        }
        User user = userMapper.toUser(requestDto);
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);

        return emailVerificationService.generateAndSave(user);
    }
}
