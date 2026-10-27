-- This is the third Flyway migration, it makes the categories table.
-- Categories are things like Salary, Rent and Food and every user has their own set.
CREATE TABLE categories (
    -- The database counts up the id by itself, same as the other tables.
    id BIGSERIAL PRIMARY KEY,
    -- The user this category belongs to, if the user gets deleted then their categories go with them.
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- The name like Food, 100 characters max which matches the @Size in CategoryRequest.
    name VARCHAR(100) NOT NULL,
    -- Whether it's money coming in or going out, the CHECK means only these 2 values are allowed.
    type VARCHAR(10) NOT NULL CHECK ( type IN ('INCOME', 'EXPENSE'))

);

-- Stops one user having two categories with the same name and type, like two Food EXPENSE categories.
-- LOWER(name) means it ignores capitals so Food and food count as the same name.
-- Different users can still both have a Food category because user_id is part of it.
-- It also works as an index on user_id so listing a user's categories is fast.
CREATE UNIQUE INDEX uq_categories_user_name_type ON categories (user_id, LOWER(name), type);