package com.luxus.tinterest.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkInfoUpdateRequestDto(
        @NotBlank
        @Size(max = 120)
        String jobTitle,

        @NotBlank
        @Size(max = 120)
        String department
) {
}
