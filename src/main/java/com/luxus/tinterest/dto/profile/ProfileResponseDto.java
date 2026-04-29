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
        String goal,
        String personalityType,
        List<String> timeSlots,
        String avatarUrl,
        List<String> interests,
        boolean hasFilledProfile
) {
}
