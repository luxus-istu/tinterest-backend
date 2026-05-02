package com.luxus.tinterest.dto.recommendation;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationFiltersDto {
    private String city;
    private String goal;
    private String department;
    private String gender;
    private String personalityType;
    private List<Long> interestIds;

    public boolean isEmpty() {
        return city == null &&
                goal == null &&
                department == null &&
                gender == null &&
                personalityType == null &&
                (interestIds == null || interestIds.isEmpty());
    }
}
