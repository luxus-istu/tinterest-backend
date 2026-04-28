ALTER TABLE users
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN job_title VARCHAR(120),
    ADD COLUMN department VARCHAR(120),
    ADD COLUMN communication_style VARCHAR(120),
    ADD COLUMN preferred_contact_method VARCHAR(120),
    ADD COLUMN meeting_preference VARCHAR(120);

CREATE TABLE interests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX uq_interests_lower_name ON interests (LOWER(name));

CREATE TABLE user_interests (
    user_id BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, interest_id),
    CONSTRAINT fk_user_interests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_interests_interest FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_interests_interest_id ON user_interests(interest_id);
