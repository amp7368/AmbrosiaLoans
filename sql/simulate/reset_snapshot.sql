EXPLAIN
UPDATE client c
SET balance_amount       = COALESCE(q.account_balance, 0),   -- account_balance might be null
    balance_last_updated = COALESCE(q.date, TO_TIMESTAMP(0)) -- account_
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               ss.account_balance,
                               ss.date
     FROM client c
              LEFT JOIN account_sim_snapshot ss ON c.id = ss.client_id
     ORDER BY c.id,
              ss.date DESC) AS q

WHERE c.id = q.id;

UPDATE loan l
SET end_date = NULL,
    status   = 'ACTIVE'
WHERE status = 'PAID'
  AND end_date >= :from_date;
SELECT *
FROM loan;