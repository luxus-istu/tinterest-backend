package com.luxus.tinterest.controller;

import com.luxus.tinterest.dto.profile.BasicProfileUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CommunicationPreferencesUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CompleteProfileRequestDto;
import com.luxus.tinterest.dto.profile.InterestsUpdateRequestDto;
import com.luxus.tinterest.dto.profile.ProfileResponseDto;
import com.luxus.tinterest.dto.profile.WorkInfoUpdateRequestDto;
import com.luxus.tinterest.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(profileService.getMyProfile(requireUserId(userId)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping("/me/complete")
    public ResponseEntity<ProfileResponseDto> completeProfile(@AuthenticationPrincipal Long userId,
                                                              @Valid @RequestBody CompleteProfileRequestDto request) {
        return ResponseEntity.ok(profileService.completeProfile(requireUserId(userId), request));
    }

    @PutMapping("/me/basic")
    public ResponseEntity<ProfileResponseDto> updateBasic(@AuthenticationPrincipal Long userId,
                                                          @Valid @RequestBody BasicProfileUpdateRequestDto request) {
        return ResponseEntity.ok(profileService.updateBasic(requireUserId(userId), request));
    }

    @PutMapping("/me/work")
    public ResponseEntity<ProfileResponseDto> updateWork(@AuthenticationPrincipal Long userId,
                                                         @Valid @RequestBody WorkInfoUpdateRequestDto request) {
        return ResponseEntity.ok(profileService.updateWork(requireUserId(userId), request));
    }

    @PutMapping("/me/communication")
    public ResponseEntity<ProfileResponseDto> updateCommunication(@AuthenticationPrincipal Long userId,
                                                                  @Valid @RequestBody CommunicationPreferencesUpdateRequestDto request) {
        return ResponseEntity.ok(profileService.updateCommunication(requireUserId(userId), request));
    }

    @PutMapping("/me/interests")
    public ResponseEntity<ProfileResponseDto> updateInterests(@AuthenticationPrincipal Long userId,
                                                              @Valid @RequestBody InterestsUpdateRequestDto request) {
        return ResponseEntity.ok(profileService.updateInterests(requireUserId(userId), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponseDto> uploadAvatar(@AuthenticationPrincipal Long userId,
                                                           @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadAvatar(requireUserId(userId), file));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new AccessDeniedException("Authenticated user is missing");
        }
        return userId;
    }
}
