package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.admin.AdminStatisticsResponseDto;
import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.repository.MatchRepository;
import com.luxus.tinterest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public AdminStatisticsResponseDto getStatistics() {
        Instant now = Instant.now();
        
        return AdminStatisticsResponseDto.builder()
                .totalUsers(userRepository.count())
                .totalMatches(matchRepository.count())
                .registrationDynamics(AdminStatisticsResponseDto.UserRegistrationDynamicsDto.builder()
                        .lastDay(userRepository.countByCreatedAtAfter(now.minus(1, ChronoUnit.DAYS)))
                        .lastWeek(userRepository.countByCreatedAtAfter(now.minus(7, ChronoUnit.DAYS)))
                        .lastMonth(userRepository.countByCreatedAtAfter(now.minus(30, ChronoUnit.DAYS)))
                        .build())
                .genderDistribution(getGenderDistribution())
                .topCities(mapResultToMap(userRepository.findTopCities(10)))
                .topInterests(mapResultToMap(userRepository.findTopInterests(10)))
                .build();
    }

    private Map<String, Long> getGenderDistribution() {
        return userRepository.countUsersByGender().stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? ((Gender) row[0]).name() : "UNKNOWN",
                        row -> (Long) row[1]
                ));
    }

    private Map<String, Long> mapResultToMap(List<Object[]> results) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }
}
