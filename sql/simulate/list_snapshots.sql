-- All snapshots in date order
SELECT date,
       COALESCE(loan_balance,
                FIRST_VALUE(loan_balance)
                OVER (PARTITION BY loan_grp ORDER BY date,event), 0)
                               AS loan_balance,
       COALESCE(invest_balance,
                FIRST_VALUE(invest_balance)
                OVER (PARTITION BY invest_grp ORDER BY date,event ), 0)
                               AS invest_balance,
       q.loan_delta / 4096.0   AS loan_delta,
       q.invest_delta / 4096.0 AS invest_delta,
       event
--/ 262144.0
FROM (
     SELECT *,
            COALESCE(SUM(CASE WHEN loan_balance IS NOT NULL THEN 1 END)
                     OVER (ORDER BY date ,event ), 0) AS loan_grp,
            COALESCE(SUM(CASE WHEN invest_balance IS NOT NULL THEN 1 END)
                     OVER (ORDER BY date ,event ), 0) AS invest_grp
     FROM (
          SELECT delta   AS invest_delta,
                 0       AS loan_delta,
                 balance AS invest_balance,
                 NULL    AS loan_balance,
                 date,
                 event,
                 client_id
          FROM client_invest_snapshot
          UNION
          SELECT 0       AS invest_delta,
                 delta   AS loan_delta,
                 NULL    AS invest_balance,
                 balance AS loan_balance,
                 date,
                 event,
                 client_id
          FROM client_loan_snapshot) q
     WHERE client_id = 311) q
         LEFT JOIN client ON client_id = client.id
ORDER BY date, event;

SELECT SUM(delta) / 4096
FROM client_invest_snapshot
WHERE date = '2024-05-23 00:00:00.000000 +00:00'
  AND event = 'PROFIT';

SELECT *
FROM client_history
WHERE id = 275
ORDER BY balance_invest_last_updated, LOWER(sys_period);

SELECT (
       SELECT SUM(ABS(amount)) / 4096.0 / 64 AS original
       FROM (
            SELECT SUM(amount) AS amount
            FROM adjust_balance
            WHERE adjust_balance.event_type IN ('ADJUST_UP', 'ADJUST_DOWN')
              AND client_id IN (
                               SELECT id
                               FROM client
                               WHERE balance_invest_amount != 0)
            GROUP BY client_id) q) original,
       (
       SELECT SUM(ABS(amount)) / 4096.0 / 64 AS original
       FROM (
            SELECT SUM(delta) AS amount
            FROM client_invest_snapshot
            WHERE client_invest_snapshot.event IN ('ADJUST_UP', 'ADJUST_DOWN')
              AND client_id IN (
                               SELECT id
                               FROM client
                               WHERE balance_invest_amount != 0)
            GROUP BY client_id) q) current;

