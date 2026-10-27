-- This is the fourth Flyway migration, it makes the transactions table.
-- A transaction is one bit of money going in or out, like getting paid or buying food.
CREATE TABLE transactions (
    -- The database counts up the id by itself, same as the other tables.
    id BIGSERIAL PRIMARY KEY,
    -- The account the money went in or out of.
    -- ON DELETE CASCADE means deleting an account deletes all its transactions too.
    account_id BIGINT NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    -- The category it's filled under, like Food.
    -- There's no cascade here on purpose so the database won't let a category be deleted while transactions still use it.
    category_id BIGINT NOT NULL REFERENCES categories (id),
    -- NUMERIC(12,2) stores the amount exactly, up to 12 digits with 2 of them after the decimal point.
    -- Money should never be floating point type because those can't store numbers like 0.10 exactly.
    -- The CHECK means the amount always has to be more than 0, the type column says if it's coming in or going out.
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    -- INCOME or EXPENSE, it has to match the type of the category it's in.
    type VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    -- An optional note like Tesco or October salary, it's allowed to be empty.
    description VARCHAR(255),
    -- The day the money moved, DATE has no time part.
    date DATE NOT NULL,
    -- When the transaction was added to the app, same as the other tables.
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()

);

-- Makes finding an account's transactions in a date range fast, which is what the list endpoint does most.
CREATE INDEX idx_transactions_account_date ON transactions (account_id, date);
-- Makes checking if a category is still being used fast, which happens before deleting a category.
CREATE INDEX idx_transactions_category_id ON transactions (category_id);