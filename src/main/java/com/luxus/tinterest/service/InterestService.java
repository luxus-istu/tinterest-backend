package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;

    @Transactional(readOnly = true)
    public List<InterestResponseDto> getInterests() {
        return interestRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private InterestResponseDto toResponse(Interest interest) {
        return new InterestResponseDto(interest.getId(), interest.getName());
    }
}
