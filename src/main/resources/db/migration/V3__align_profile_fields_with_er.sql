ALTER TABLE users
    RENAME COLUMN communication_style TO personality_type;

ALTER TABLE users
    RENAME COLUMN preferred_contact_method TO time_slots;

ALTER TABLE users
    RENAME COLUMN meeting_preference TO goal;

ALTER TABLE users
    ALTER COLUMN time_slots TYPE TEXT[]
    USING CASE
        WHEN time_slots IS NULL OR BTRIM(time_slots) = '' THEN NULL
        ELSE ARRAY[time_slots]
    END;
