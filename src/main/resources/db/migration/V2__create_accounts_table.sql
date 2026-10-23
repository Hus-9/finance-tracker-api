-- This is the second Flyway migration, it makes the accounts table.
-- It has to run after V1 because it points at the users table.
CREATE TABLE accounts (
    -- The database counts up the id by itself, same as the users table.
    id BIGSERIAL PRIMARY KEY,
    -- The user this account belongs to, REFERENCES means it has to be the id of a real user.
    -- ON DELETE CASCADE means if a user gets deleted all their accounts get deleted with them.
    -- There's no endpoint to delete users yet but this means it'll work properly if one gets added.
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- 100 characters max which matches the @Size in AccountRequest and the length on the Account entity.
    name VARCHAR(100) NOT NULL,
    -- The CHECK means the database only accepts these 3 values, the same ones as the AccountType
    -- If you add a new value to AccountType you need a new migration to change this check too or saving it will fail.
    type VARCHAR(20) NOT NULL CHECK ( type IN ('CURRENT', 'SAVINGS', 'CREDIT')),
    -- The 3 letter currency code like GBP
    currency VARCHAR(3) NOT NULL,
    -- When the account was made, same as in the users table.
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

 -- An index on user_id so finding all of one user's accounts is fast, which is what the list endpoint does.
 -- Postgres doesn't make an index for foreign keys by itself so we have to add it ourselves.
CREATE INDEX idx_accounts_user_id ON accounts (user_id);