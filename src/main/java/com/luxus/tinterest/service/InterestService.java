package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.interest.InterestResponseDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.exception.admin.InterestAlreadyExistsException;
import com.luxus.tinterest.exception.admin.InterestNotFoundException;
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

    @Transactional
    public InterestResponseDto addInterest(String name) {

        name = name.trim();

        if (interestRepository.existsByNameIgnoreCase(name)) {
            throw new InterestAlreadyExistsException("Interest already exists");
        }

        Interest newInterest = new Interest();
        newInterest.setName(name);
        return toResponse(interestRepository.save(newInterest));
    }

    @Transactional
    public void deleteInterest(Long interestId) {
        if (!interestRepository.existsById(interestId)) {
            throw new InterestNotFoundException("Interest not found");
        }
        interestRepository.deleteById(interestId);
    }

    private InterestResponseDto toResponse(Interest interest) {
        return new InterestResponseDto(interest.getId(), interest.getName());
    }
}
