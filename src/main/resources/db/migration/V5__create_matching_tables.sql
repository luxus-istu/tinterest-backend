CREATE TABLE user_swipes (
    id SERIAL PRIMARY KEY,
    from_user_id INT REFERENCES users(id),
    to_user_id INT REFERENCES users(id),
    reaction VARCHAR(10) CHECK (reaction IN ('LIKE', 'DISLIKE')),
    swiped_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE matches (
    id SERIAL PRIMARY KEY,
    user_id_1 INT REFERENCES users(id),
    user_id_2 INT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE recommendation_offsets (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    offset_value INT NOT NULL DEFAULT 0,
    cycle INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_feed_offset_user UNIQUE (user_id)
);

CREATE INDEX idx_swipe_from_reaction_date
    ON user_swipes(from_user_id, reaction, swiped_at);

CREATE INDEX idx_swipe_to_from_reaction
    ON user_swipes(to_user_id, from_user_id, reaction);

CREATE INDEX idx_match_user1 ON matches(user_id_1);
CREATE INDEX idx_match_user2 ON matches(user_id_2);