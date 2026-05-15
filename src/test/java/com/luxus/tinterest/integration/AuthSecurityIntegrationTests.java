package com.luxus.tinterest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.login.LoginRequestDto;
import com.luxus.tinterest.dto.registration.RegistrationRequestDto;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.enums.Role;
import com.luxus.tinterest.repository.EmailVerificationRepository;
import com.luxus.tinterest.repository.UserRepository;
import com.luxus.tinterest.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RecommendationService recommendationService;

    @BeforeEach
    void cleanDatabase() {
        emailVerificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a user with valid payload")
    void shouldRegisterUser() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto(
                "John",
                "Doe",
                null,
                "john.doe@example.com",
                "securePass123",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                "en"
        );

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.message").value("Verification code has been sent"));
    }

    @Test
    @DisplayName("Should not register a user with invalid input")
    void shouldReturnBadRequestForInvalidRegistration() throws Exception {
        RegistrationRequestDto request = new RegistrationRequestDto(
                "",
                "",
                null,
                "invalid-email",
                "short",
                LocalDate.now().plusDays(1),
                null,
                "xx"
        );

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("Should login verified user and return JWT access token")
    void shouldLoginVerifiedUser() throws Exception {
        User user = createVerifiedUser("verified@example.com", Role.USER);
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto("verified@example.com", "securePass123");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("Should reject invalid login credentials")
    void shouldRejectInvalidLogin() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("missing@example.com", "wrongPass");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Should return unauthorized for protected endpoint without JWT")
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return forbidden when authenticated user has wrong role")
    void shouldReturnForbiddenForWrongRole() throws Exception {
        User user = createVerifiedUser("user@example.com", Role.USER);
        userRepository.save(user);

        String token = obtainAccessToken("user@example.com", "securePass123");

        mockMvc.perform(get("/v1/admin/statistics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found for missing profile")
    void shouldReturnNotFoundForMissingProfile() throws Exception {
        User user = createVerifiedUser("profile@example.com", Role.USER);
        userRepository.save(user);

        String token = obtainAccessToken("profile@example.com", "securePass123");

        mockMvc.perform(get("/v1/profiles/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    private User createVerifiedUser(String email, Role role) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .emailVerified(true)
                .passwordHash(passwordEncoder.encode("securePass123"))
                .role(role)
                .blocked(false)
                .createdAt(Instant.now())
                .build();
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto(email, password);

        String response = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
