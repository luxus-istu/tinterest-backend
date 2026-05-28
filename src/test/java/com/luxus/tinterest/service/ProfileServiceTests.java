package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.profile.BasicProfileUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CompleteProfileRequestDto;
import com.luxus.tinterest.dto.profile.ProfileResponseDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.profile.UnknownInterestsException;
import com.luxus.tinterest.repository.InterestRepository;
import com.luxus.tinterest.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Unit Tests")
class ProfileServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private MinioStorageService minioStorageService;

    @InjectMocks
    private ProfileService profileService;

    @Test
    @DisplayName("Should get profile by user id")
    void testGetProfileReturnsResponse() {
        User user = User.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .city("Moscow")
                .interests(Set.of(Interest.builder().id(1L).name("Music").build()))
                .build();

        when(userRepository.findWithInterestsById(1L)).thenReturn(Optional.of(user));

        ProfileResponseDto response = profileService.getProfile(1L);

        assertEquals(1L, response.id());
        assertEquals("Ivan", response.firstName());
        assertEquals(List.of("Music"), response.interests());
    }

    @Test
    @DisplayName("Should throw when profile user is missing")
    void testGetProfileThrowsWhenUserMissing() {
        when(userRepository.findWithInterestsById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> profileService.getProfile(1L));
    }

    @Test
    @DisplayName("Should throw when complete profile contains unknown interests")
    void testCompleteProfileThrowsWhenUnknownInterests() {
        User user = User.builder().id(1L).build();
        when(userRepository.findWithInterestsById(1L)).thenReturn(Optional.of(user));
        when(interestRepository.findAllByNormalizedNames(any())).thenReturn(List.of(
            Interest.builder().id(1L).name("Sports").build()
        ));

        CompleteProfileRequestDto request = new CompleteProfileRequestDto(
                "Moscow",
                "About me",
                "Developer",
                "IT",
                "Learn",
                "Introvert",
                List.of("Evenings"),
                List.of("Sports", "Art")
        );

        UnknownInterestsException exception = assertThrows(UnknownInterestsException.class,
                () -> profileService.completeProfile(1L, request));

        assertTrue(exception.getMessage().contains("Art"));
    }

    @Test
    @DisplayName("Should upload avatar and save returned url")
    void testUploadAvatarUsesMinioStorage() {
        User user = User.builder().id(1L).avatarUrl(null).build();
        when(userRepository.findWithInterestsById(1L)).thenReturn(Optional.of(user));
        when(minioStorageService.uploadAvatar(any(), eq(1L), eq(null))).thenReturn("https://public/avatars/1/file.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = profileService.uploadAvatar(1L, null);

        assertEquals("https://public/avatars/1/file.jpg", response.avatarUrl());
    }
}
