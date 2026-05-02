package com.luxus.tinterest.repository;


import com.luxus.tinterest.entity.User;
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
}
