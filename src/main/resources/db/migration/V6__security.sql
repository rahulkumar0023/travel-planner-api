-- --- V2 security migration start ---
UPDATE trips   SET owner = 'rahul0083.be@gmail.com' WHERE owner IS NULL OR owner = '' OR owner = 'anon';
UPDATE budgets SET owner = 'rahul0083.be@gmail.com' WHERE owner IS NULL OR owner = '' OR owner = 'anon';
UPDATE trips SET participants = array_append(COALESCE(participants, ARRAY[]::varchar[]), 'rahul0083.be@gmail.com')
WHERE owner = 'rahul0083.be@gmail.com';

-- --- V2 security migration end ---
