ALTER TABLE expenses
    ADD COLUMN IF NOT EXISTS currency varchar(3) NOT NULL DEFAULT 'EUR';

ALTER TABLE trips
    ADD COLUMN IF NOT EXISTS spend_currencies text;
