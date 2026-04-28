package com.luxus.tinterest.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunicationPreferencesUpdateRequestDto(
        @NotBlank
        @Size(max = 120)
        String communicationStyle,

        @NotBlank
        @Size(max = 120)
        String preferredContactMethod,

        @NotBlank
        @Size(max = 120)
        String meetingPreference
) {
}
