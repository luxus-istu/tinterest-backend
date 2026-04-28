package com.luxus.tinterest.dto.profile;

import com.luxus.tinterest.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BasicProfileUpdateRequestDto(
        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @Size(max = 100)
        String middleName,

        @NotNull
        @Past
        LocalDate dateOfBirth,

        @NotNull
        Gender gender,

        @NotBlank
        @Pattern(regexp = "ru|en")
        String language,

        @NotBlank
        @Size(max = 100)
        String city,

        @Size(max = 1000)
        String about
) {
}
