package com.luxus.tinterest.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailResendRequestDto {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
}
