package com.luxus.tinterest.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailResendRequestDto {

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

}
