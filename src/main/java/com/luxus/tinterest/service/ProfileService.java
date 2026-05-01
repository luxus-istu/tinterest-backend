package com.luxus.tinterest.service;

import com.luxus.tinterest.dto.profile.BasicProfileUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CommunicationPreferencesUpdateRequestDto;
import com.luxus.tinterest.dto.profile.CompleteProfileRequestDto;
import com.luxus.tinterest.dto.profile.InterestsUpdateRequestDto;
import com.luxus.tinterest.dto.profile.ProfileResponseDto;
import com.luxus.tinterest.dto.profile.WorkInfoUpdateRequestDto;
import com.luxus.tinterest.entity.Interest;
import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.exception.common.UserNotFoundException;
import com.luxus.tinterest.exception.profile.UnknownInterestsException;
import com.luxus.tinterest.repository.InterestRepository;
import com.luxus.tinterest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final MinioStorageService minioStorageService;

    @Transactional(readOnly = true)
    public ProfileResponseDto getMyProfile(Long userId) {
        return toProfileResponse(loadUserWithInterests(userId));
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long userId) {
        return toProfileResponse(loadUserWithInterests(userId));
    }

    @Transactional
    public ProfileResponseDto completeProfile(Long userId, CompleteProfileRequestDto request) {
        User user = loadUserWithInterests(userId);
        applyQuestionnaireInfo(user, request.city(), request.about());
        applyWorkInfo(user, request.jobTitle(), request.department());
        applyCommunicationPreferences(user, request.goal(), request.personalityType(), request.timeSlots());
        replaceInterests(user, request.interests());
        user.setHasFilledProfile(true);
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponseDto updateBasic(Long userId, BasicProfileUpdateRequestDto request) {
        User user = loadUserWithInterests(userId);
        applyBasicInfo(user, request.firstName(), request.lastName(), request.middleName(), request.dateOfBirth(),
                request.gender(), request.language(), request.city(), request.about());
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponseDto updateWork(Long userId, WorkInfoUpdateRequestDto request) {
        User user = loadUserWithInterests(userId);
        applyWorkInfo(user, request.jobTitle(), request.department());
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponseDto updateCommunication(Long userId, CommunicationPreferencesUpdateRequestDto request) {
        User user = loadUserWithInterests(userId);
        applyCommunicationPreferences(user, request.goal(), request.personalityType(), request.timeSlots());
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponseDto updateInterests(Long userId, InterestsUpdateRequestDto request) {
        User user = loadUserWithInterests(userId);
        replaceInterests(user, request.interests());
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponseDto uploadAvatar(Long userId, MultipartFile file) {
        User user = loadUserWithInterests(userId);
        user.setAvatarUrl(minioStorageService.uploadAvatar(file, userId, user.getAvatarUrl()));
        return toProfileResponse(userRepository.save(user));
    }

    private User loadUserWithInterests(Long userId) {
        return userRepository.findWithInterestsById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void applyBasicInfo(User user, String firstName, String lastName, String middleName,
                                LocalDate dateOfBirth, com.luxus.tinterest.entity.Gender gender,
                                String language, String city, String about) {
        user.setFirstName(cleanValue(firstName));
        user.setLastName(cleanValue(lastName));
        user.setMiddleName(cleanOptionalValue(middleName));
        user.setDateOfBirth(dateOfBirth);
        user.setGender(gender);
        user.setLanguage(language);
        user.setCity(cleanValue(city));
        user.setAbout(cleanOptionalValue(about));
    }

    private void applyQuestionnaireInfo(User user, String city, String about) {
        user.setCity(cleanValue(city));
        user.setAbout(cleanOptionalValue(about));
    }

    private void applyWorkInfo(User user, String jobTitle, String department) {
        user.setJobTitle(cleanValue(jobTitle));
        user.setDepartment(cleanValue(department));
    }

    private void applyCommunicationPreferences(User user, String goal, String personalityType, List<String> timeSlots) {
        user.setGoal(cleanValue(goal));
        user.setPersonalityType(cleanValue(personalityType));
        user.setTimeSlots(cleanValues(timeSlots));
    }

    private void replaceInterests(User user, List<String> requestedInterests) {
        LinkedHashMap<String, String> normalizedToDisplay = requestedInterests.stream()
                .map(this::cleanValue)
                .collect(Collectors.toMap(
                        name -> name.toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<Interest> existingInterests = interestRepository.findAllByNormalizedNames(normalizedToDisplay.keySet());
        Map<String, Interest> existingByNormalizedName = existingInterests.stream()
                .collect(Collectors.toMap(
                        interest -> interest.getName().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));

        List<String> unknownInterests = new ArrayList<>();
        for (Map.Entry<String, String> entry : normalizedToDisplay.entrySet()) {
            if (!existingByNormalizedName.containsKey(entry.getKey())) {
                unknownInterests.add(entry.getValue());
            }
        }

        if (!unknownInterests.isEmpty()) {
            throw new UnknownInterestsException(unknownInterests);
        }

        Set<Interest> orderedInterests = new LinkedHashSet<>();
        for (String normalizedName : normalizedToDisplay.keySet()) {
            orderedInterests.add(existingByNormalizedName.get(normalizedName));
        }
        user.setInterests(orderedInterests);
    }

    private ProfileResponseDto toProfileResponse(User user) {
        List<String> interests = user.getInterests().stream()
                .map(Interest::getName)
                .toList();

        return new ProfileResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getLanguage(),
                user.getCity(),
                user.getAbout(),
                user.getJobTitle(),
                user.getDepartment(),
                user.getGoal(),
                user.getPersonalityType(),
                user.getTimeSlots() == null ? List.of() : List.copyOf(user.getTimeSlots()),
                user.getAvatarUrl(),
                interests,
                user.isHasFilledProfile()
        );
    }

    private String cleanValue(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String cleanOptionalValue(String value) {
        String cleaned = cleanValue(value);
        return cleaned == null || cleaned.isBlank() ? null : cleaned;
    }

    private List<String> cleanValues(List<String> values) {
        LinkedHashMap<String, String> normalizedToDisplay = values.stream()
                .map(this::cleanValue)
                .collect(Collectors.toMap(
                        value -> value.toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return List.copyOf(normalizedToDisplay.values());
    }
}
