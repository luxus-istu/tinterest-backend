package com.luxus.tinterest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxus.tinterest.dto.profile.BasicProfileUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CommunicationPreferencesUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CompleteProfileRequestDto;
import com.luxus.tinterest.dto.profile.InterestsUpdateRequestDto;
import com.luxus.tinterest.dto.profile.ProfileResponseDto;
import com.luxus.tinterest.dto.profile.WorkInfoUpdateRequestDto;
import com.luxus.tinterest.entity.Gender;
import com.luxus.tinterest.exception.GlobalExceptionHandler;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.handler.ProfileHandler;
import com.luxus.tinterest.exception.profile.InvalidAvatarFileException;
import com.luxus.tinterest.service.ProfileService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;


@ExtendWith(MockitoExtension.class)
@DisplayName("Profile Controller Tests")
class ProfileControllerTests {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ProfileResponseDto validProfileResponse;
    private CompleteProfileRequestDto validCompleteRequest;
    private BasicProfileUpdateRequestDto validBasicUpdateRequest;
    private WorkInfoUpdateRequestDto validWorkUpdateRequest;
    private CommunicationPreferencesUpdateRequestDto validCommunicationUpdateRequest;
    private InterestsUpdateRequestDto validInterestsUpdateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler(), new ProfileHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup test data
        validProfileResponse = new ProfileResponseDto(
                1L,
                "John",
                "Doe",
                "Michael",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                "en",
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday", "Wednesday", "Friday"),
                "https://example.com/avatar.jpg",
                List.of("Java", "Spring Boot"),
                true
        );

        validCompleteRequest = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday", "Wednesday", "Friday"),
                List.of("Java", "Spring Boot")
        );

        validBasicUpdateRequest = new BasicProfileUpdateRequestDto(
                "John",
                "Doe",
                "Michael",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                "en",
                "New York",
                "Software developer"
        );

        validWorkUpdateRequest = new WorkInfoUpdateRequestDto(
                "Senior Developer",
                "Engineering"
        );

        validCommunicationUpdateRequest = new CommunicationPreferencesUpdateRequestDto(
                "Career advancement",
                "ENTJ",
                List.of("Monday", "Wednesday", "Friday")
        );

        validInterestsUpdateRequest = new InterestsUpdateRequestDto(
                List.of("Java", "Spring Boot", "Microservices")
        );
    }

    private void mockAuthentication(Long userId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // GET /v1/profiles/me
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve own profile")
    void testGetMyProfileSuccess() throws Exception {
        Long userId = 1L;
        when(profileService.getMyProfile(userId)).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(get("/v1/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.goal").value("Career advancement"))
                .andExpect(jsonPath("$.personalityType").value("ENTJ"))
                .andExpect(jsonPath("$.interests[0]").value("Java"));
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for get own profile")
    void testGetMyProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/profiles/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user profile not found")
    void testGetMyProfileNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException()).when(profileService).getMyProfile(userId);

        mockAuthentication(userId);

        mockMvc.perform(get("/v1/profiles/me"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /v1/profiles/{userId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully retrieve user profile by ID")
    void testGetProfileByIdSuccess() throws Exception {
        Long userId = 2L;
        when(profileService.getProfile(userId)).thenReturn(validProfileResponse);

        mockMvc.perform(get("/v1/profiles/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("Should return 404 when profile by ID not found")
    void testGetProfileByIdNotFound() throws Exception {
        Long userId = 999L;
        doThrow(new UserNotFoundException()).when(profileService).getProfile(userId);

        mockMvc.perform(get("/v1/profiles/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/complete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully complete profile")
    void testCompleteProfileSuccess() throws Exception {
        Long userId = 1L;
        when(profileService.completeProfile(userId, validCompleteRequest)).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCompleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("New York"))
                .andExpect(jsonPath("$.goal").value("Career advancement"))
                .andExpect(jsonPath("$.hasFilledProfile").value(true));
    }

    @Test
    @DisplayName("Should return 400 when city is missing on complete profile")
    void testCompleteProfileWithoutCity() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                null,
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday"),
                List.of("Java")
        );
        mockAuthentication(userId);
        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when city exceeds max length on complete profile")
    void testCompleteProfileWithCityTooLong() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "A".repeat(101),
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday"),
                List.of("Java")
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when jobTitle is missing on complete profile")
    void testCompleteProfileWithoutJobTitle() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                null,
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday"),
                List.of("Java")
        );

        mockAuthentication(userId);
        
        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when goal is missing on complete profile")
    void testCompleteProfileWithoutGoal() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                null,
                "ENTJ",
                List.of("Monday"),
                List.of("Java")
        );

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when personalityType is missing on complete profile")
    void testCompleteProfileWithoutPersonalityType() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                null,
                List.of("Monday"),
                List.of("Java")
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when timeSlots list is empty on complete profile")
    void testCompleteProfileWithoutTimeSlots() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of(),
                List.of("Java")
        );

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when timeSlots exceeds max size on complete profile")
    void testCompleteProfileWithTooManyTimeSlots() throws Exception {
        Long userId = 1L;
        List<String> tooManyTimeSlots = List.of(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday", "Sunday", "Morning", "Afternoon", "Evening", "Night"
        );
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                tooManyTimeSlots,
                List.of("Java")
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when interests list is empty on complete profile")
    void testCompleteProfileWithEmptyInterests() throws Exception {
        Long userId = 1L;
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday"),
                List.of()
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when interests exceeds max size on complete profile")
    void testCompleteProfileWithTooManyInterests() throws Exception {
        Long userId = 1L;
        List<String> tooManyInterests = List.of(
                "Interest1", "Interest2", "Interest3", "Interest4", "Interest5",
                "Interest6", "Interest7", "Interest8", "Interest9", "Interest10",
                "Interest11", "Interest12", "Interest13", "Interest14", "Interest15",
                "Interest16", "Interest17", "Interest18", "Interest19", "Interest20", "Interest21"
        );
        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "New York",
                "Software developer",
                "Senior Developer",
                "Engineering",
                "Career advancement",
                "ENTJ",
                List.of("Monday"),
                tooManyInterests
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for complete profile")
    void testCompleteProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCompleteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found on complete profile")
    void testCompleteProfileUserNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException()).when(profileService).completeProfile(userId, validCompleteRequest);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCompleteRequest)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/basic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update basic profile info")
    void testUpdateBasicProfileSuccess() throws Exception {
        Long userId = 1L;
        when(profileService.updateBasic(userId, validBasicUpdateRequest)).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBasicUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("Should return 400 when firstName is missing on basic profile update")
    void testUpdateBasicProfileWithoutFirstName() throws Exception {
        Long userId = 1L;
        BasicProfileUpdateRequestDto request = new BasicProfileUpdateRequestDto(
                null,
                "Doe",
                "Michael",
                LocalDate.of(1990, 5, 15),
                Gender.MALE,
                "en",
                "New York",
                "Software developer"
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for basic profile update")
    void testUpdateBasicProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/v1/profiles/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBasicUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found on basic profile update")
    void testUpdateBasicProfileUserNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException()).when(profileService).updateBasic(userId, validBasicUpdateRequest);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/basic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBasicUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/work
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update work info")
    void testUpdateWorkInfoSuccess() throws Exception {
        Long userId = 1L;
        when(profileService.updateWork(userId, validWorkUpdateRequest)).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validWorkUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Senior Developer"));
    }

    @Test
    @DisplayName("Should return 400 when jobTitle is missing on work info update")
    void testUpdateWorkInfoWithoutJobTitle() throws Exception {
        Long userId = 1L;
        WorkInfoUpdateRequestDto request = new WorkInfoUpdateRequestDto(null, "Engineering");

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when department is missing on work info update")
    void testUpdateWorkInfoWithoutDepartment() throws Exception {
        Long userId = 1L;
        WorkInfoUpdateRequestDto request = new WorkInfoUpdateRequestDto("Senior Developer", null);

        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for work info update")
    void testUpdateWorkInfoWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validWorkUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found on work info update")
    void testUpdateWorkInfoUserNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException()).when(profileService).updateWork(userId, validWorkUpdateRequest);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/work")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validWorkUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/communication
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update communication preferences")
    void testUpdateCommunicationPreferencesSuccess() throws Exception {
        Long userId = 1L;
        when(profileService.updateCommunication(userId, validCommunicationUpdateRequest)).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCommunicationUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goal").value("Career advancement"))
                .andExpect(jsonPath("$.personalityType").value("ENTJ"));
    }

    @Test
    @DisplayName("Should return 400 when goal is missing on communication preferences update")
    void testUpdateCommunicationPreferencesWithoutGoal() throws Exception {
        Long userId = 1L;
        CommunicationPreferencesUpdateRequestDto request = new CommunicationPreferencesUpdateRequestDto(
                null,
                "ENTJ",
                List.of("Monday")
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when personalityType is missing on communication preferences update")
    void testUpdateCommunicationPreferencesWithoutPersonalityType() throws Exception {
        Long userId = 1L;
        CommunicationPreferencesUpdateRequestDto request = new CommunicationPreferencesUpdateRequestDto(
                "Career advancement",
                null,
                List.of("Monday")
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when timeSlots list is empty on communication preferences update")
    void testUpdateCommunicationPreferencesWithoutTimeSlots() throws Exception {
        Long userId = 1L;
        CommunicationPreferencesUpdateRequestDto request = new CommunicationPreferencesUpdateRequestDto(
                "Career advancement",
                "ENTJ",
                List.of()
        );

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for communication preferences update")
    void testUpdateCommunicationPreferencesWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCommunicationUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found on communication preferences update")
    void testUpdateCommunicationPreferencesUserNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException()).when(profileService).updateCommunication(userId, validCommunicationUpdateRequest);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/communication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCommunicationUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /v1/profiles/me/interests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully update interests")
    void testUpdateInterestsSuccess() throws Exception {
        Long userId = 1L;
        when(profileService.updateInterests(userId, validInterestsUpdateRequest)).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validInterestsUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interests[0]").value("Java"));
    }

    @Test
    @DisplayName("Should return 400 when interests list is empty on interests update")
    void testUpdateInterestsWithEmptyList() throws Exception {
        Long userId = 1L;
        InterestsUpdateRequestDto request = new InterestsUpdateRequestDto(List.of());

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when interests exceeds max size on interests update")
    void testUpdateInterestsWithTooManyInterests() throws Exception {
        Long userId = 1L;
        List<String> tooManyInterests = List.of(
                "Interest1", "Interest2", "Interest3", "Interest4", "Interest5",
                "Interest6", "Interest7", "Interest8", "Interest9", "Interest10",
                "Interest11", "Interest12", "Interest13", "Interest14", "Interest15",
                "Interest16", "Interest17", "Interest18", "Interest19", "Interest20", "Interest21"
        );
        InterestsUpdateRequestDto request = new InterestsUpdateRequestDto(tooManyInterests);

        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for interests update")
    void testUpdateInterestsWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validInterestsUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found on interests update")
    void testUpdateInterestsUserNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException()).when(profileService).updateInterests(userId, validInterestsUpdateRequest);

        mockAuthentication(userId);

        mockMvc.perform(put("/v1/profiles/me/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validInterestsUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /v1/profiles/me/avatar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should successfully upload avatar")
    void testUploadAvatarSuccess() throws Exception {
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test file content".getBytes()
        );

        when(profileService.uploadAvatar(eq(userId), any())).thenReturn(validProfileResponse);

        mockAuthentication(userId);

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"));
    }

    @Test
    @DisplayName("Should return 400 when avatar file is invalid")
    void testUploadAvatarWithInvalidFile() throws Exception {
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "invalid file content".getBytes()
        );

        doThrow(new InvalidAvatarFileException("Invalid file type")).when(profileService).uploadAvatar(eq(userId), any());

        mockAuthentication(userId);

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for avatar upload")
    void testUploadAvatarWithoutAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test file content".getBytes()
        );

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when user not found on avatar upload")
    void testUploadAvatarUserNotFound() throws Exception {
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test file content".getBytes()
        );

        doThrow(new UserNotFoundException()).when(profileService).uploadAvatar(eq(userId), any());

        mockAuthentication(userId);

        mockMvc.perform(multipart("/v1/profiles/me/avatar")
                .file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when avatar file is missing on avatar upload")
    void testUploadAvatarWithoutFile() throws Exception {
        Long userId = 1L;

        mockAuthentication(userId);

        mockMvc.perform(multipart("/v1/profiles/me/avatar"))
                .andExpect(status().isBadRequest());
    }
}
