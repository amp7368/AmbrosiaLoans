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
WHERE event = 'ADJUST_UP'
   OR event = 'ADJUST_DOWN'
    AND client_id = 117
GROUP BY client_id
ORDER BY delta_le DESC;

SELECT account_delta / 4096   delta_le,
       account_balance / 4096 balance_le,
       event,
       date
FROM client_snapshot
WHERE client_id = 117;


SELECT *
FROM loan
WHERE client_id = 117;
SELECT *
FROM loan_payment
WHERE loan_id = 129;