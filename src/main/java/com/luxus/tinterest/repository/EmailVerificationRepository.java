package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.EmailVerification;
import com.luxus.tinterest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    Optional<EmailVerification> findByUser(User user);
}
