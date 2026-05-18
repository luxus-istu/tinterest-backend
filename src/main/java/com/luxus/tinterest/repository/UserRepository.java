package com.luxus.tinterest.repository;


import com.luxus.tinterest.entity.User;
import com.luxus.tinterest.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail( String email);

    Optional<User> findByEmail(String email);

    @Query("select distinct u from User u left join fetch u.interests where u.id = :userId")
    Optional<User> findWithInterestsById(@Param("userId") Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.interests WHERE u.id IN :ids")
    List<User> findAllByIdWithInterests(@Param("ids") List<Long> ids);

    Page<User> findAllByRole(Role role, Pageable pageable);

    Page<User> findAllByRoleAndEmailStartingWithIgnoreCase(Role role, String email, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.emailVerified = true AND (" +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countByCreatedAtAfter(@Param("since") java.time.Instant since);

    @Query("SELECT u.gender, COUNT(u) FROM User u GROUP BY u.gender")
    List<Object[]> countUsersByGender();

    @Query(value = "SELECT city, COUNT(*) as count FROM users WHERE city IS NOT NULL GROUP BY city ORDER BY count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopCities(@Param("limit") int limit);

    @Query(value = "SELECT i.name, COUNT(ui.user_id) as count FROM interests i " +
            "JOIN user_interests ui ON i.id = ui.interest_id " +
            "GROUP BY i.name ORDER BY count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopInterests(@Param("limit") int limit);
}
