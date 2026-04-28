package com.luxus.tinterest.dto.profile;

import com.luxus.tinterest.entity.Gender;

import java.time.LocalDate;
import java.util.List;

public record ProfileResponseDto(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        LocalDate dateOfBirth,
        Gender gender,
        String language,
        String city,
        String about,
        String jobTitle,
        String department,
        String communicationStyle,
        String preferredContactMethod,
        String meetingPreference,
        String avatarUrl,
        List<String> interests,
        boolean hasFilledProfile
) {
}
