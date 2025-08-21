-- --- V2 security migration start ---
UPDATE trips   SET owner = 'rahul0083.be@gmail.com' WHERE owner IS NULL OR owner = '' OR owner = 'anon';
UPDATE budgets SET owner = 'rahul0083.be@gmail.com' WHERE owner IS NULL OR owner = '' OR owner = 'anon';

-- --- V2 security migration end ---
