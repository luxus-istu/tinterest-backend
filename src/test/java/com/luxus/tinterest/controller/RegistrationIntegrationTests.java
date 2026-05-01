package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.entity.Gender;
import com.luxus.tinterest.entity.Role;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.repository.EmailVerificationRepository;
import com.luxus.tinterest.repository.UserRepository;

import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Скорее всего не выполнятся из-за:
// В модели User столбец time_slots странно настроен, исправьте как вам надо
// - QA

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Registration Integration Tests")
class RegistrationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private RegistrationRequestDto validRequest;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    public void showDdlAuto() {
        Object value = emf.getProperties().get("hibernate.hbm2ddl.auto");
        System.out.println("Effective ddl-auto: " + value);
    }

    @BeforeEach
    void setUp() {
        emailVerificationRepository.deleteAll();
        userRepository.deleteAll();

        validRequest = new RegistrationRequestDto();
        validRequest.setFirstName("Integration");
        validRequest.setLastName("Test");
        validRequest.setMiddleName("User");
        validRequest.setEmail("integration@test.com");
        validRequest.setPassword("testPass123");
        validRequest.setDateOfBirth(LocalDate.of(1985, 3, 10));
        validRequest.setGender(Gender.MALE);
        validRequest.setLanguage("en");
    }

    @Test
    @DisplayName("Should register user and persist to database")
    void testUserPersistsToDatabase() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(validRequest.getFirstName(), savedUser.getFirstName());
        assertEquals(validRequest.getLastName(), savedUser.getLastName());
        assertEquals(validRequest.getEmail(), savedUser.getEmail());
    }

    @Test
    @DisplayName("Should set user role to USER after registration")
    void testUserRoleIsSetToUser() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    @DisplayName("Should not verify email immediately after registration")
    void testEmailNotVerifiedAfterRegistration() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertFalse(savedUser.isEmailVerified());
    }

    @Test
    @DisplayName("Should encode password before storing")
    void testPasswordIsEncoded() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertNotEquals(validRequest.getPassword(), savedUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testDuplicateEmailRegistrationFails() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should store user with all provided fields")
    void testAllUserFieldsStored() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(validRequest.getFirstName(), savedUser.getFirstName());
        assertEquals(validRequest.getLastName(), savedUser.getLastName());
        assertEquals(validRequest.getMiddleName(), savedUser.getMiddleName());
        assertEquals(validRequest.getDateOfBirth(), savedUser.getDateOfBirth());
        assertEquals(validRequest.getGender(), savedUser.getGender());
        assertEquals(validRequest.getLanguage(), savedUser.getLanguage());
    }

    @Test
    @DisplayName("Should set created timestamp on registration")
    void testCreatedAtSetOnRegistration() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should return correct response DTO")
    void testResponseDtoIsCorrect() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(validRequest.getEmail()))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void testInvalidEmailFormatRejected() throws Exception {
        validRequest.setEmail("invalid-email-format");

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        boolean userExists = userRepository.existsByEmail("invalid-email-format");
        assertFalse(userExists);
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void testShortPasswordRejected() throws Exception {
        validRequest.setPassword("short");

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept registration with different languages")
    void testDifferentLanguagesAccepted() throws Exception {
        validRequest.setEmail("ru-user@test.com");
        validRequest.setLanguage("ru");

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail("ru-user@test.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("ru", savedUser.getLanguage());
    }

    @Test
    @DisplayName("Should handle registration with maximum field lengths")
    void testMaximumFieldLengths() throws Exception {
        validRequest.setFirstName("a".repeat(100));
        validRequest.setLastName("b".repeat(100));
        validRequest.setMiddleName("c".repeat(100));

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(100, savedUser.getFirstName().length());
    }

    @Test
    @DisplayName("Should not block user after registration")
    void testUserNotBlockedAfterRegistration() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertFalse(savedUser.isBlocked());
    }

    @Test
    @DisplayName("Should mark profile as not filled after registration")
    void testProfileNotFilledAfterRegistration() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(validRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertFalse(savedUser.isHasFilledProfile());
    }
}
