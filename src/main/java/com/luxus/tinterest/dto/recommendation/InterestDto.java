package com.luxus.tinterest.dto.recommendation;

import com.luxus.tinterest.entity.Interest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterestDto {

    private Long id;
    private String name;

    public static InterestDto from(Interest interest) {
        return InterestDto.builder()
                .id(interest.getId())
                .name(interest.getName())
                .build();
    }
}
