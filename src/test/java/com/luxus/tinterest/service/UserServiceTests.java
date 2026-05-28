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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private RegistrationRequestDto requestDto;
    private User mappedUser;

    @BeforeEach
    void setUp() {
        requestDto = new RegistrationRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("password123");

        mappedUser = User.builder()
                .email("user@example.com")
                .build();
    }

    @Test
    @DisplayName("Should register a new user and generate verification code")
    void testRegisterGeneratesVerificationCode() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userMapper.toUser(requestDto)).thenReturn(mappedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailVerificationService.generateAndSave(mappedUser)).thenReturn("verification-code");

        String code = userService.register(requestDto);

        assertEquals("verification-code", code);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("encoded-password", saved.getPasswordHash());
        assertEquals(Role.USER, saved.getRole());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Should throw when email already registered")
    void testRegisterThrowsWhenAlreadyRegistered() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(UserAlreadyRegisteredException.class,
                () -> userService.register(requestDto));
    }

    @Test
    @DisplayName("Should login authenticated user")
    void testLoginReturnsUser() {
        User user = User.builder()
                .email("user@example.com")
                .emailVerified(true)
                .blocked(false)
                .passwordHash("encoded-password")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        User result = userService.login("user@example.com", "password123");

        assertSame(user, result);
    }

    @Test
    @DisplayName("Should throw invalid credentials when email is missing")
    void testLoginThrowsWhenUserMissing() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login("user@example.com", "password123"));
    }

    @Test
    @DisplayName("Should throw invalid credentials when password does not match")
    void testLoginThrowsWhenPasswordMismatch() {
        User user = User.builder()
                .email("user@example.com")
                .emailVerified(true)
                .blocked(false)
                .passwordHash("encoded-password")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login("user@example.com", "wrong-password"));
    }

    @Test
    @DisplayName("Should throw when email is not verified")
    void testLoginThrowsWhenEmailNotVerified() {
        User user = User.builder()
                .email("user@example.com")
                .emailVerified(false)
                .blocked(false)
                .passwordHash("encoded-password")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailNotVerifiedException.class,
                () -> userService.login("user@example.com", "password123"));
    }

    @Test
    @DisplayName("Should throw when user is blocked")
    void testLoginThrowsWhenUserBlocked() {
        User user = User.builder()
                .email("user@example.com")
                .emailVerified(true)
                .blocked(true)
                .passwordHash("encoded-password")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(UserBlockedException.class,
                () -> userService.login("user@example.com", "password123"));
    }

    @Test
    @DisplayName("Should find user by id")
    void testFindByIdReturnsUser() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertSame(user, result);
    }

    @Test
    @DisplayName("Should throw when user id is missing")
    void testFindByIdThrowsWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.findById(1L));
    }
}
