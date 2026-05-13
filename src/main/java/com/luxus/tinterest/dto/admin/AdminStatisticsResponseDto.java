package com.luxus.tinterest.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminStatisticsResponseDto {
    private long totalUsers;
    private long totalMatches;
    private UserRegistrationDynamicsDto registrationDynamics;
    private Map<String, Long> genderDistribution;
    private Map<String, Long> topCities;
    private Map<String, Long> topInterests;

    @Data
    @Builder
    public static class UserRegistrationDynamicsDto {
        private long lastDay;
        private long lastWeek;
        private long lastMonth;
    }
}
