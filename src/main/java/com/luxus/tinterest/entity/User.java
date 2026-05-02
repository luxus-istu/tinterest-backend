package com.luxus.tinterest.entity;

import com.luxus.tinterest.enums.Gender;
import com.luxus.tinterest.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String language;

    private String city;

    private String about;

    @Column(name = "job_title")
    private String jobTitle;

    private String department;

    private String goal;

    @Column(name = "personality_type")
    private String personalityType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "time_slots", columnDefinition = "text[]")
    private List<String> timeSlots;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String email;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "has_filled_profile")
    private boolean hasFilledProfile = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    @OrderBy("name ASC")
    private Set<Interest> interests = new LinkedHashSet<>();

    private boolean blocked = false;

    @Column(name = "created_at")
    private Instant createdAt;
}
