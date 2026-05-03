package com.luxus.tinterest.dto.recommendation;

import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class UserCardDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String city;
    private String about;
    private String jobTitle;
    private String department;
    private String goal;
    private String personalityType;
    private List<String> timeSlots;
    private String avatarUrl;
    private Set<InterestDto> interests;

    public static UserCardDto from(User user) {
        return UserCardDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .city(user.getCity())
                .about(user.getAbout())
                .jobTitle(user.getJobTitle())
                .department(user.getDepartment())
                .goal(user.getGoal())
                .personalityType(user.getPersonalityType())
                .timeSlots(user.getTimeSlots())
                .avatarUrl(user.getAvatarUrl())
                .interests(user.getInterests().stream()
                        .map(InterestDto::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }
}