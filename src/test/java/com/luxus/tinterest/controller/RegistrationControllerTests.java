package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.exception.handler.RegistrationHandler;
import com.luxus.tinterest.exception.registration.UserAlreadyRegisteredException;
import com.luxus.tinterest.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registration Controller Tests")
class RegistrationControllerTests {

    @Mock
    private AuthService authService;

    @InjectMocks
    private RegistrationController registrationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegistrationRequestDto validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController)
                .setControllerAdvice(new RegistrationHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        validRequest = new RegistrationRequestDto();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setMiddleName("Michael");
        validRequest.setEmail("john@example.com");
        validRequest.setPassword("securePassword123");
        validRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/register - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully register new user")
    void testSuccessfulRegistration() throws Exception {
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.message").value("Verification code has been sent"));
    }

    @Test
    @DisplayName("Should allow registration without authentication")
    void testRegistrationWithoutAuthentication() throws Exception {
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    // -------------------------------------------------------------------------
    // POST /v1/auth/register - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when firstName is missing")
    void testRegistrationWithoutFirstName() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when lastName is missing")
    void testRegistrationWithoutLastName() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setFirstName("John");
        request.setEmail("john@example.com");
        request.setPassword("password");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email is missing")
    void testRegistrationWithoutEmail() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("password");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is missing")
    void testRegistrationWithoutPassword() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void testRegistrationWithInvalidEmail() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setPassword("password");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is too short")
    void testRegistrationWithShortPassword() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("short");
        request.setDateOfBirth(LocalDate.of(1990, 1, 15));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when user with email already exists")
    void testRegistrationWithExistingEmail() throws Exception {
        doThrow(new UserAlreadyRegisteredException()).when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }
}
