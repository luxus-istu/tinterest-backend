package com.luxus.tinterest.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompleteProfileRequestDto(
        @NotBlank
        @Size(max = 100)
        String city,

        @Size(max = 1000)
        String about,

        @NotBlank
        @Size(max = 120)
        String jobTitle,

        @NotBlank
        @Size(max = 120)
        String department,

        @NotBlank
        @Size(max = 120)
        String goal,

        @NotBlank
        @Size(max = 120)
        String personalityType,

        @NotEmpty
        @Size(max = 10)
        List<@NotBlank @Size(max = 50) String> timeSlots,

        @NotEmpty
        @Size(max = 20)
        List<@NotBlank @Size(max = 50) String> interests
) {
}
