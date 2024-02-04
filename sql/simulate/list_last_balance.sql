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

SELECT q1.client_id, q1.event, q1.delta_le, COUNT(q2.delta_le) total_change
FROM (
     SELECT gen_random_uuid()         id,
            client_id,
            event,
            SUM(account_delta) / 4096 delta_le
     FROM client_snapshot
     WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
     GROUP BY client_id, event
     ORDER BY delta_le) q1
         INNER JOIN (
                    SELECT client_id,
                           event,
                           SUM(account_delta) / 4096 delta_le
                    FROM client_snapshot
                    WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
                    GROUP BY client_id, event
                    ORDER BY delta_le) q2 ON q1.client_id = q2.client_id
GROUP BY q1.delta_le, q1.event, q1.client_id
HAVING COUNT(q2.delta_le) > 1
ORDER BY MAX(q2.delta_le), q1.client_id;

SELECT client_id,
       event,
       SUM(account_delta) / 4096 delta_le
FROM client_snapshot
WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
GROUP BY client_id, event
ORDER BY delta_le;


SELECT account_delta   delta,
       account_balance balance,
       event,
       date
FROM client_snapshot
WHERE client_id = 139
ORDER BY date;

SELECT *, amount / 4096
FROM investment
WHERE event_type = 'ADJUST_UP'
  AND client_id = 225;

SELECT *
FROM loan
WHERE id = 115;

SELECT *
FROM loan_payment
WHERE loan_id = 115;