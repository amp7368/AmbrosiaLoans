SELECT (SUM(loan_le) - 95) / 64 total_loaned,
       SUM(invest_le) / 64      total_invested
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               c.minecraft_username,
                               ss.invest_balance / 4096                  invest_le,
                               ss.loan_balance / 4096                    loan_le,
                               (ss.invest_balance + loan_balance) / 4096 balance_le,
                               ss.date
     FROM client c
              LEFT JOIN client_snapshot ss ON c.id = ss.client_id
     ORDER BY c.id,
              ss.date DESC,
              event DESC) q;


SELECT q1.client_id, q1.event, q1.delta_le, COUNT(q2.delta_le) total_change
FROM (
     SELECT gen_random_uuid()                     id,
            client_id,
            event,
            SUM(loan_delta + invest_delta) / 4096 delta_le
     FROM client_snapshot
     WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
     GROUP BY client_id, event
     ORDER BY delta_le) q1
         INNER JOIN (
                    SELECT client_id,
                           event,
                           SUM(loan_delta + invest_delta) / 4096 delta_le
                    FROM client_snapshot
                    WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
                    GROUP BY client_id, event
                    ORDER BY delta_le) q2 ON q1.client_id = q2.client_id
GROUP BY q1.delta_le, q1.event, q1.client_id
HAVING COUNT(q2.delta_le) > 1
ORDER BY MAX(q2.delta_le), q1.client_id;


-- All
SELECT event,
       SUM(delta) / 4096 / 64 delta_stx
FROM (
     SELECT *
     FROM client_invest_snapshot
     UNION ALL
     SELECT *
     FROM client_loan_snapshot) q
-- WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
GROUP BY event
ORDER BY delta_stx;

-- by client
SELECT client_id,
       minecraft_username,
       event,
       SUM(loan_delta + invest_delta) / 4096 delta_le
FROM client_snapshot
         LEFT JOIN client ON client.id = client_snapshot.client_id
WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN', 'ADJUST_LOAN')
GROUP BY client_id, event, minecraft_username
-- HAVING ABS(SUM(loan_delta + invest_delta) / 4096) > 32
ORDER BY event, delta_le;

SELECT ROUND((loan_delta + invest_delta) / 4096.0, 3) delta_le,
       ROUND(loan_balance / 4096.0, 3)                loan_le,
       event,
       ROUND(invest_balance / 4096.0, 3)              invest_le,
       date
FROM client_snapshot
WHERE client_id = 117
ORDER BY date;
SELECT *
FROM loan
WHERE client_id = 117;
SELECT *
FROM loan_section
WHERE loan_id = 129;


SELECT date('2022-09-08 00:00:00.000000 +00:00') - date('2022-05-10 04:00:00.000000 +00:00');

SELECT *
FROM (
     SELECT DISTINCT ON (c.id) c.id,
                               c.minecraft_username,
                               ss.invest_balance / 4096                  invest_le,
                               ss.loan_balance / 4096                    loan_le,
                               (ss.invest_balance + loan_balance) / 4096 balance_le,
                               ss.date
     FROM client c
              LEFT JOIN client_snapshot ss ON c.id = ss.client_id
     ORDER BY c.id,
              ss.date DESC,
              event DESC) q
ORDER BY balance_le;

