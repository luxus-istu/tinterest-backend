package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.profile.BasicProfileUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CommunicationPreferencesUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CompleteProfileRequestDto;
import com.luxus.tinterest.dto.profile.InterestsUpdateRequestDto;
import com.luxus.tinterest.dto.profile.ProfileResponseDto;
import com.luxus.tinterest.dto.profile.WorkInfoUpdateRequestDto;
import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.handler.ProfileHandler;
import com.luxus.tinterest.exception.profile.InvalidAvatarFileException;
import com.luxus.tinterest.service.ProfileService;

import io.lettuce.core.dynamic.support.MethodParameter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
@DisplayName("Profile Controller Tests")
class ProfileControllerTests {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new ProfileHandler(), new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .defaultRequest(get("/").with(authorized()))
                .addFilters(new AuthorizationHeaderFilter())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private void mockAuthenticationPrincipal(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private RequestPostProcessor authorized() {
        return request -> {
            request.addHeader("Authorization", "Bearer dummy-token");
            return request;
        };
    }

    private RequestPostProcessor unauthorized() {
        return request -> {
            request.removeHeader("Authorization");
            return request;
        };
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ProfileResponseDto createSampleProfile(Long userId) {
    return new ProfileResponseDto(
            userId,
            "John",
            "Doe",
            null,
            LocalDate.of(1993, 5, 15),
            Gender.MALE,
            "English",
            "New York",
            "Loves hiking and coffee",
            "Engineer",
            "IT",
            "Professional growth",
            "INTJ",
            List.of(),
            "john.jpg",
            List.of(),
            true
    );
}

    // -------------------------------------------------------------------------
    // GET /v1/profiles/me - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve current user's profile")
    void testGetMyProfileSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        
        ProfileResponseDto profile = createSampleProfile(1L);
        when(profileService.getMyProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/v1/profiles/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.jobTitle").value("Engineer"));
    }

    // -------------------------------------------------------------------------
    // GET /v1/profiles/me - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when user profile not found")
    void testGetMyProfileNotFound() throws Exception {
        mockAuthenticationPrincipal(999L);
        doThrow(new UserNotFoundException()).when(profileService).getMyProfile(999L);

        mockMvc.perform(get("/v1/profiles/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when retrieving current profile without authentication")
    void testGetMyProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/profiles/me").with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /v1/profiles/{userId} - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve user profile by id")
    void testGetUserProfileSuccess() throws Exception {
        ProfileResponseDto profile = createSampleProfile(2L);
        when(profileService.getProfile(2L)).thenReturn(profile);

        mockMvc.perform(get("/v1/profiles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    // -------------------------------------------------------------------------
    // GET /v1/profiles/{userId} - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 404 when user profile not found by id")
    void testGetUserProfileNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(profileService).getProfile(999L);

        mockMvc.perform(get("/v1/profiles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should retrieve user profile by id without authentication")
    void testGetUserProfileWithoutAuthentication() throws Exception {
        ProfileResponseDto profile = createSampleProfile(999L);
        when(profileService.getProfile(999L)).thenReturn(profile);

        mockMvc.perform(get("/v1/profiles/999").with(unauthorized()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(999))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/complete - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully complete profile")
    void testCompleteProfileSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);

        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
            "New York",
            "Loves hiking and coffee",
            "Engineer",
            "IT",
            "Professional growth",
            "INTJ",
            List.of("10:00-12:00", "14:00-16:00"),
            List.of("Reading", "Music")
        );

        ProfileResponseDto response = createSampleProfile(1L);

        when(profileService.completeProfile(eq(1L), any(CompleteProfileRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/v1/profiles/me/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.jobTitle").value("Engineer"))
                .andExpect(jsonPath("$.gender").value("MALE"));
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/complete - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when city is missing")
    void testCompleteProfileWithoutName() throws Exception {
        mockAuthenticationPrincipal(1L);
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
            null,
            "Loves hiking and coffee",
            "Engineer",
            "IT",
            "Professional growth",
            "INTJ",
            List.of("10:00-12:00", "14:00-16:00"),List.of("Reading", "Music")
        );

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for invalid profile data")
    void testCompleteProfileInvalidData() throws Exception {
        mockAuthenticationPrincipal(1L);
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Loves hiking and coffee",
                "Engineer",
                "IT",
                "Professional growth",
                "INTJ",
                List.of("10:00-12:00", "14:00-16:00"),List.of("Reading", "Music")
        );
        
        doThrow(new HttpMessageNotReadableException("Invalid request format")).when(profileService)
                .completeProfile(eq(1L), any(CompleteProfileRequestDto.class));

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when completing profile without authentication")
    void testCompleteProfileWithoutAuthentication() throws Exception {
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Loves hiking and coffee",
                "Engineer",
                "IT",
                "Professional growth",
                "INTJ",
                List.of("10:00-12:00", "14:00-16:00"),List.of("Reading", "Music")
        );

        mockMvc.perform(put("/v1/profiles/me/complete").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/basic - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update basic profile")
    void testUpdateBasicProfileSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        BasicProfileUpdateRequestDto request = new BasicProfileUpdateRequestDto(
            "Jane",
            "Doe",
            null,
            LocalDate.of(1990, 1, 15),
            Gender.FEMALE,
            "en",
            "Los Angeles",
            "Software developer"
        );
        ProfileResponseDto response = createSampleProfile(1L);
        
        when(profileService.updateBasic(eq(1L), any(BasicProfileUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/v1/profiles/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/basic - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when name is missing in basic update")
    void testUpdateBasicProfileWithoutName() throws Exception {
        mockAuthenticationPrincipal(1L);
        BasicProfileUpdateRequestDto request = new BasicProfileUpdateRequestDto(
            null,
            "Doe",
            null,
            LocalDate.of(1990, 1, 15),
            Gender.FEMALE,
            "en",
            "Los Angeles",
            "Software developer"
        );

        mockMvc.perform(put("/v1/profiles/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when updating basic profile without authentication")
    void testUpdateBasicProfileWithoutAuthentication() throws Exception {
        BasicProfileUpdateRequestDto request = new BasicProfileUpdateRequestDto(
            "Jane",
            "Doe",
            null,
            LocalDate.of(1990, 1, 15),
            Gender.FEMALE,
            "en",
            "Los Angeles",
            "Software developer"
        );

        mockMvc.perform(put("/v1/profiles/me/basic").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/work - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update work profile")
    void testUpdateWorkProfileSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        WorkInfoUpdateRequestDto request = new WorkInfoUpdateRequestDto("Senior Engineer", "Tech Corp");
        ProfileResponseDto response = createSampleProfile(1L);
        when(profileService.updateWork(eq(1L), any(WorkInfoUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/work - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when profession is missing")
    void testUpdateWorkProfileWithoutProfession() throws Exception {
        mockAuthenticationPrincipal(1L);
        WorkInfoUpdateRequestDto request = new WorkInfoUpdateRequestDto(null, "Tech Corp");

        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when updating work profile without authentication")
    void testUpdateWorkProfileWithoutAuthentication() throws Exception {
        WorkInfoUpdateRequestDto request = new WorkInfoUpdateRequestDto("Senior Engineer", "Tech Corp");

        mockMvc.perform(put("/v1/profiles/me/work").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/communication - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update communication profile")
    void testUpdateCommunicationProfileSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        CommunicationPreferencesUpdateRequestDto request = new CommunicationPreferencesUpdateRequestDto(
                "goal", "personality type", List.of("10:00-12:00", "14:00-16:00")
        );
        ProfileResponseDto response = createSampleProfile(1L);
        when(profileService.updateCommunication(eq(1L), any(CommunicationPreferencesUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 401 when updating communication profile without authentication")
    void testUpdateCommunicationProfileWithoutAuthentication() throws Exception {
        CommunicationPreferencesUpdateRequestDto request = new CommunicationPreferencesUpdateRequestDto(
                "goal", "personality type", List.of("10:00-12:00", "14:00-16:00")
        );

        mockMvc.perform(put("/v1/profiles/me/communication").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/interests - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update user interests")
    void testUpdateInterestsSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        InterestsUpdateRequestDto request = new InterestsUpdateRequestDto(
            List.of("Music", "Sport")
        );
        ProfileResponseDto response = createSampleProfile(1L);
        when(profileService.updateInterests(eq(1L), any(InterestsUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/interests - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when interests list is empty")
    void testUpdateInterestsEmpty() throws Exception {
        mockAuthenticationPrincipal(1L);
        InterestsUpdateRequestDto request = new InterestsUpdateRequestDto(List.of());

        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when updating interests without authentication")
    void testUpdateInterestsWithoutAuthentication() throws Exception {
        InterestsUpdateRequestDto request = new InterestsUpdateRequestDto(List.of("Music", "Sport"));

        mockMvc.perform(put("/v1/profiles/me/interests").with(unauthorized())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /v1/profiles/me/avatar - Positive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully upload avatar")
    void testUploadAvatarSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fake image content".getBytes());
        ProfileResponseDto response = createSampleProfile(1L);
        when(profileService.uploadAvatar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should successfully upload avatar with png format")
    void testUploadAvatarPngSuccess() throws Exception {
        mockAuthenticationPrincipal(1L);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png",
                MediaType.IMAGE_PNG_VALUE, "fake image content".getBytes());
        ProfileResponseDto response = createSampleProfile(1L);
        when(profileService.uploadAvatar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // POST /v1/profiles/me/avatar - Negative
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should return 400 when no file is provided")
    void testUploadAvatarNoFile() throws Exception {
        mockAuthenticationPrincipal(1L);

        mockMvc.perform(multipart("/v1/profiles/me/avatar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for unsupported file format")
    void testUploadAvatarUnsupportedFormat() throws Exception {
        mockAuthenticationPrincipal(1L);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf",
                MediaType.APPLICATION_PDF_VALUE, "fake pdf content".getBytes());

        doThrow(new InvalidAvatarFileException("Unsupported file format"))
            .when(profileService).uploadAvatar(eq(1L), any());

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 413 when file size exceeds limit")
    void testUploadAvatarFileSizeExceeded() throws Exception {
        mockAuthenticationPrincipal(1L);
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE, largeContent);
        doThrow(new InvalidAvatarFileException("Invalid avatar file")).when(profileService).uploadAvatar(eq(1L), any());

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when uploading avatar without authentication")
    void testUploadAvatarWithoutAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fake image content".getBytes());

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file).with(unauthorized()))
                .andExpect(status().isUnauthorized());
    }

    private static final class AuthorizationHeaderFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String uri = httpRequest.getRequestURI();

            if (uri.startsWith("/v1/profiles/me") && httpRequest.getHeader("Authorization") == null) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            chain.doFilter(request, response);
        }
    }
}
