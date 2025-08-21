-- --- V2 security migration start ---
UPDATE trips   SET owner = 'rahul0083.be@gmail.com' WHERE owner IS NULL OR owner = '' OR owner = 'anon';
UPDATE budgets SET owner = 'rahul0083.be@gmail.com' WHERE owner IS NULL OR owner = '' OR owner = 'anon';

-- Add yourself as participant on every trip you own (skip if already present)
INSERT INTO trip_participants (trip_id, participants)
SELECT t.id, 'rahul0083.be@gmail.com'
FROM trips t
         LEFT JOIN trip_participants tp
                   ON tp.trip_id = t.id AND tp.participants = 'rahul0083.be@gmail.com'
WHERE t.owner = 'rahul0083.be@gmail.com'
  AND tp.trip_id IS NULL;


-- --- V2 security migration end ---
