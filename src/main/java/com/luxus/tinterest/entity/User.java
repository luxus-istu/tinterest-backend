package com.luxus.tinterest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

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

    private String about;

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

    private boolean blocked = false;

    @Column(name = "created_at")
    private Instant createdAt;
}