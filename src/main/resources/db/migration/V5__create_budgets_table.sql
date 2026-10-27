-- This is the fifth  Flyway migration, it makes the budgets table.
-- A budget is a spending limit for one category in one month, like 200 pounds on Food in October 2026
CREATE TABLE budgets (
    -- The database counts up the id itself same as the other tables.
    id BIGSERIAL PRIMARY KEY,
    -- The user this budget belongs to, if the user gets deleted their budgets go too.
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- The category the limit is for, if the category gets deleted its budgets go with it.
    category_id BIGINT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    -- The month saved as text like 2026-10, the CHECK makes sure it's a real year and month.
    month VARCHAR(7) NOT NULL CHECK (month ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    -- The spending limit, stored exactly like the transaction amounts and it has to be more than 0.
    limit_amount NUMERIC(12,2) NOT NULL CHECK (limit_amount > 0),
    -- A user can only have one budget per category per month.
    CONSTRAINT uq_budgets_user_category_month UNIQUE (user_id, category_id, month)

);

-- Makes getting one user's budgets for a month fast, which the budget list and the budget report both do.
CREATE INDEX idx_budgets_user_month ON budgets (user_id, month);