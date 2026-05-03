package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.UserSwipe;
import com.luxus.tinterest.enums.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSwipeRepository extends JpaRepository<UserSwipe, Long> {

    boolean existsByFromUserIdAndToUserIdAndReaction(
            Long fromUserId,
            Long toUserId,
            Reaction reaction
    );
}
