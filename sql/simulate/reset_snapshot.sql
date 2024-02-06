EXPLAIN
UPDATE client c
SET balance_loan_amount   = COALESCE(q.loan_balance, 0),      -- account_balance might be null
    balance_invest_amount = COALESCE(q.invest_balance, 0),    -- account_balance might be null
    balance_last_updated  = COALESCE(q.date, TO_TIMESTAMP(0)) -- date might be null
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               ss.invest_balance,
                               ss.loan_balance,
                               ss.date
     FROM client c
              LEFT JOIN client_snapshot ss ON c.id = ss.client_id
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