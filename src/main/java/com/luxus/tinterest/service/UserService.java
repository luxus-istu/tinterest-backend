package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.enums.Role;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.login.EmailNotVerifiedException;
import com.luxus.tinterest.exception.login.InvalidCredentialsException;
import com.luxus.tinterest.exception.login.UserBlockedException;
import com.luxus.tinterest.exception.registration.UserAlreadyRegisteredException;
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

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (!user.isEmailVerified()) throw new EmailNotVerifiedException();

        if (user.isBlocked()) throw new UserBlockedException();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new InvalidCredentialsException();

        return user;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}
