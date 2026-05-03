package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.RecommendationOffset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationOffsetRepository extends JpaRepository<RecommendationOffset, Long> {

    Optional<RecommendationOffset> findByUserId(Long userId);

    @Modifying
    @Query("""
        UPDATE RecommendationOffset r
        SET r.offsetValue = :offset,
            r.cycle = :cycle,
            r.updatedAt = CURRENT_TIMESTAMP
        WHERE r.userId = :userId
        """)
    void updateOffsetAndCycle(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("cycle") int cycle
    );
}