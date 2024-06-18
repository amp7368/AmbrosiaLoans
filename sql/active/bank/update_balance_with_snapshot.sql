UPDATE client c
SET balance_loan_amount        = COALESCE(loan_balance, 0),              -- account_balance might be null
    balance_loan_last_updated  = COALESCE(loan_date, TO_TIMESTAMP(0)),   -- date might be null
    balance_invest_amount      = COALESCE(invest_balance, 0),            -- account_balance might be null
    balance_invest_last_updated= COALESCE(invest_date, TO_TIMESTAMP(0)), -- date might be null
    balance_amount             = COALESCE(loan_balance, 0) + COALESCE(invest_balance, 0)

FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               cl.balance loan_balance,
                               cl.date    loan_date
     FROM client c
              LEFT JOIN client_loan_snapshot cl ON c.id = cl.client_id
     ORDER BY c.id,
              cl.date DESC,
              cl.event DESC) AS q1
         LEFT JOIN
     (
     SELECT DISTINCT ON (c.id) c.id,
                               ci.balance invest_balance,
                               ci.date    invest_date
     FROM client c
              LEFT JOIN client_invest_snapshot ci ON c.id = ci.client_id
     ORDER BY c.id,
              ci.date DESC,
              ci.event DESC) q2 ON q1.id = q2.id
WHERE c.id = q1.id;

