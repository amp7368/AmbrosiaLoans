SELECT *
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               ss.account_balance / 4096 balance_le,
                               ss.date
     FROM client c
              LEFT JOIN client_snapshot ss ON c.id = ss.client_id
     ORDER BY c.id,
              ss.date DESC) q
WHERE NOT EXISTS(
                SELECT 1
                FROM loan
                WHERE loan.client_id = q.id)
ORDER BY q.balance_le;

SELECT client_id,
       SUM(account_delta) / 4096 delta_le
FROM client_snapshot
WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN')
--    OR event = 'ADJUST_DOWN'
--     AND client_id = 117
GROUP BY client_id
ORDER BY delta_le DESC;

SELECT *
FROM loan
WHERE client_id = 132;

SELECT account_delta   delta,
       account_balance balance,
       event,
       date
FROM client_snapshot
WHERE client_id = 132
ORDER BY date;

SELECT *, amount / 4096
FROM investment
WHERE event_type = 'ADJUST_UP'
  AND client_id = 225;

SELECT *
FROM loan
WHERE client_id = 148;

SELECT *
FROM loan_payment
WHERE loan_id = 129;