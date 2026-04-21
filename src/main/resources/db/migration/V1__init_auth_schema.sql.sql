CREATE TABLE users (
    id SERIAL PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),

    date_of_birth DATE,

    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),

    language VARCHAR(10),

    about TEXT,
    avatar_url TEXT,

    email VARCHAR(255) NOT NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    password_hash TEXT NOT NULL,

    has_filled_profile BOOLEAN NOT NULL DEFAULT FALSE,

    role VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'ADMIN')) DEFAULT 'USER',

    blocked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id BIGINT NOT NULL,

    token_hash TEXT NOT NULL UNIQUE,

    family_id UUID NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    used BOOLEAN NOT NULL DEFAULT FALSE,

    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens(family_id);

CREATE TABLE email_verifications (
    id SERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    code_hash TEXT NOT NULL,

    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    attempts INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uq_email_verifications_user_id UNIQUE (user_id),
    CONSTRAINT fk_email_verifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);