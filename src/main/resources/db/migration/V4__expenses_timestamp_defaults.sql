-- ===== expenses timestamps default now() start =====
ALTER TABLE expenses 
  ALTER COLUMN created_at SET DEFAULT now(),
  ALTER COLUMN updated_at SET DEFAULT now();
-- ===== expenses timestamps default now() end =====
