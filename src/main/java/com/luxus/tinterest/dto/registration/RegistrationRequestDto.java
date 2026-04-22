package com.luxus.tinterest.dto.registration;


import com.luxus.tinterest.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegistrationRequestDto {

    @NotBlank(message = "Имя не может быть пустым")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустым")
    private String lastName;

    private String middleName;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Пароль должен быть минимум 8 символов")
    private String password;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private Gender gender;

    @NotBlank
    @Pattern(regexp = "ru|en")
    private String language;
}
