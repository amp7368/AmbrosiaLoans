EXPLAIN
UPDATE client c
SET balance = COALESCE(q.account_balance, 0) -- account_balance might be null
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               ss.account_balance
     FROM client c
              LEFT JOIN account_sim_snapshot ss ON c.id = ss.client_id
     ORDER BY c.id,
              ss.date DESC) AS q
WHERE c.id = q.id;

SELECT *
FROM account_sim_snapshot
ORDER BY date;

SELECT *
FROM loan_payment;
SELECT *
FROM invest;

SELECT *
FROM client;