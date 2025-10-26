-- 👇 Add notes column to trips
ALTER TABLE trips ADD COLUMN notes TEXT;

-- 👇 New table for expense tags
CREATE TABLE expense_tags (
    expense_id VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
);
