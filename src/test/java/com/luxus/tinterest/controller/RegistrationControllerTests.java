package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.dto.registration.RegistrationResponseDto;
import com.luxus.tinterest.entity.Gender;
import com.luxus.tinterest.exception.registration.UserAlreadyRegisteredException;
import com.luxus.tinterest.exception.handler.RegistrationHandler;
import com.luxus.tinterest.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController)
            .setControllerAdvice(new RegistrationHandler())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register modules for Java 8 date/time support

        validRequest = new RegistrationRequestDto();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setMiddleName("Michael");
        validRequest.setEmail("john@example.com");
        validRequest.setPassword("securePassword123");
        validRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        validRequest.setGender(Gender.MALE);
        validRequest.setLanguage("en");
    }

    @Test
    @DisplayName("Should successfully register user with valid data")
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
    @DisplayName("Should return 400 when email is missing")
    void testRegistrationWithoutEmail() throws Exception {
        validRequest.setEmail(null);

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void testRegistrationWithInvalidEmailFormat() throws Exception {
        validRequest.setEmail("invalid-email");

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is too short")
    void testRegistrationWithShortPassword() throws Exception {
        validRequest.setPassword("short");

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when password is missing")
    void testRegistrationWithoutPassword() throws Exception {
        validRequest.setPassword(null);

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when first name is missing")
    void testRegistrationWithoutFirstName() throws Exception {
        validRequest.setFirstName(null);

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when last name is missing")
    void testRegistrationWithoutLastName() throws Exception {
        validRequest.setLastName(null);

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when first name exceeds max length")
    void testRegistrationWithTooLongFirstName() throws Exception {
        validRequest.setFirstName("a".repeat(101));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when last name exceeds max length")
    void testRegistrationWithTooLongLastName() throws Exception {
        validRequest.setLastName("a".repeat(101));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when middle name exceeds max length")
    void testRegistrationWithTooLongMiddleName() throws Exception {
        validRequest.setMiddleName("a".repeat(101));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when date of birth is in the future")
    void testRegistrationWithFutureDateOfBirth() throws Exception {
        validRequest.setDateOfBirth(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when language has invalid format")
    void testRegistrationWithInvalidLanguage() throws Exception {
        validRequest.setLanguage("invalid");

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept registration with language 'ru'")
    void testRegistrationWithRussianLanguage() throws Exception {
        validRequest.setLanguage("ru");
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle user already registered exception")
    void testRegistrationWithAlreadyRegisteredEmail() throws Exception {
        doThrow(new UserAlreadyRegisteredException()).when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should accept registration without optional middle name")
    void testRegistrationWithoutMiddleName() throws Exception {
        validRequest.setMiddleName(null);
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept registration without optional gender")
    void testRegistrationWithoutGender() throws Exception {
        validRequest.setGender(null);
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept registration without optional language")
    void testRegistrationWithoutLanguage() throws Exception {
        validRequest.setLanguage(null);
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept registration with valid password of 8 characters")
    void testRegistrationWithMinimumPasswordLength() throws Exception {
        validRequest.setPassword("pass1234");
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Response should contain correct email")
    void testResponseContainsCorrectEmail() throws Exception {
        doNothing().when(authService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void testRegistrationWithEmptyBody() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
