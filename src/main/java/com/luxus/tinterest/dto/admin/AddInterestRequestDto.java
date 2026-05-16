package com.luxus.tinterest.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddInterestRequestDto {
    @NotBlank(message = "Field name is required")
    private String name;
}
