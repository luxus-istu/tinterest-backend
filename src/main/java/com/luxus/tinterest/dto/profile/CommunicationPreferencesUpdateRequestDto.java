package com.luxus.tinterest.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CommunicationPreferencesUpdateRequestDto(
        @NotBlank
        @Size(max = 120)
        String goal,

        @NotBlank
        @Size(max = 120)
        String personalityType,

        @NotEmpty
        @Size(max = 10)
        List<@NotBlank @Size(max = 50) String> timeSlots
) {
}
