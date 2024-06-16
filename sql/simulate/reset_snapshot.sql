EXPLAIN
UPDATE client c
SET balance_loan_amount   = COALESCE(q.loan_balance, 0),      -- account_balance might be null
    balance_invest_amount = COALESCE(q.invest_balance, 0),    -- account_balance might be null
    balance_last_updated  = COALESCE(q.date, TO_TIMESTAMP(0)) -- date might be null
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               cs.invest_balance,
                               cs.loan_balance,
                               cs.date
     FROM client c
              LEFT JOIN client_snapshot cs ON c.id = cs.client_id
     ORDER BY c.id,
              cs.date DESC,
              cs.event DESC) AS q
WHERE c.id = q.id;

UPDATE loan l
SET end_date = NULL,
    status   = 'ACTIVE'
WHERE status = 'PAID'
  AND end_date >= :from_date;
