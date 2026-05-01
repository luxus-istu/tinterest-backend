package com.luxus.tinterest.entity;

import com.luxus.tinterest.enums.Reaction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_swipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSwipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "to_user_id")
    private Long toUserId;

    @Enumerated(EnumType.STRING)
    @Column
    private Reaction reaction;

    @Column(name = "swiped_at")
    private LocalDateTime swipedAt = LocalDateTime.now();
}