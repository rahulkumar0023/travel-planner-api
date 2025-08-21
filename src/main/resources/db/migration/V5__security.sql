-- --- V2 security migration start ---
ALTER TABLE trips   ADD COLUMN IF NOT EXISTS owner       varchar(255) DEFAULT 'anon';
ALTER TABLE budgets ADD COLUMN IF NOT EXISTS owner       varchar(255) DEFAULT 'anon';
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS created_by varchar(255) DEFAULT 'anon';

CREATE INDEX IF NOT EXISTS idx_trips_owner       ON trips(owner);
CREATE INDEX IF NOT EXISTS idx_budgets_owner     ON budgets(owner);
CREATE INDEX IF NOT EXISTS idx_expenses_created_by ON expenses(created_by);
-- --- V2 security migration end ---
