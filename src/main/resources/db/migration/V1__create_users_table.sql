-- This is the first Flyway migration, it makes the users table.
-- Flyway runs these files in order by the number after the V and only runs each one once.
-- Once this has run, don't edit it. If the table needs changing, make a new file like V3 instead or Flyway will complain the file changed.
CREATE TABLE users (
    -- BIGSERIAL makes the database count up the id by itself for every new user, it's the same as IDENTITY in the User entity.
    id BIGSERIAL PRIMARY KEY ,
    -- UNIQUE means the database won't let two users have the same email, even if two people sign up at the exact same time.
    email VARCHAR(255) NOT NULL UNIQUE ,
    -- The BCrypt hash of the password, a BCrypt hash is only 60 characters so 255 is plenty.
    password_hash VARCHAR(255) NOT NULL ,
    -- TIMESTAMPTZ saves the exact moment in time which matches Instant in Java.
    -- DEFAULT now() fills it in if nothing gets sent but the entity's @PrePersist already sets it anyway.
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);