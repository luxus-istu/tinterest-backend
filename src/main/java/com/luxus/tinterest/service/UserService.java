package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.admin.UserSummaryResponseDto;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.enums.Role;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.admin.InvalidAdminOperationException;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.login.EmailNotVerifiedException;
import com.luxus.tinterest.exception.login.InvalidCredentialsException;
import com.luxus.tinterest.exception.login.UserBlockedException;
import com.luxus.tinterest.exception.registration.UserAlreadyRegisteredException;
import com.luxus.tinterest.mapper.UserMapper;
import com.luxus.tinterest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<UserSummaryResponseDto> getAllUsers(String email, Pageable pageable) {
        Page<User> users;
        if (email != null && !email.isBlank()) {
            users = userRepository.findAllByRoleAndEmailStartingWithIgnoreCase(Role.USER, email, pageable);
        } else {
            users = userRepository.findAllByRole(Role.USER, pageable);
        }
        return users.map(userMapper::toUserSummaryResponseDto);
    }

    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() == Role.ADMIN) {
            throw new InvalidAdminOperationException("Cannot block user with ADMIN role");
        }
        user.setBlocked(true);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() == Role.ADMIN) {
            throw new InvalidAdminOperationException("Cannot unblock user with ADMIN role");
        }
        user.setBlocked(false);
        userRepository.save(user);
    }
}
