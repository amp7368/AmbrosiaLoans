-- sum bank profits
SELECT balance / 4096 / 64.0 stx, date
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;

-- sum events
SELECT cs.event                    event_type,
       SUM(cs.delta) / 4096 / 64.0 total_stx
FROM client c
         LEFT JOIN (
                   SELECT *
                   FROM client_invest_snapshot
                   UNION ALL
                   SELECT *
                   FROM client_loan_snapshot) cs ON c.id = cs.client_id
GROUP BY event_type
ORDER BY event_type;

-- sum loan interest over time
SELECT TO_CHAR(DATE_TRUNC('MONTH', date), 'MM/DD/YY') AS date_pretty,
       ROUND(SUM(cs.delta) / 4096 / 64.0, 3)             total_stx
FROM (
     SELECT *
     FROM client_invest_snapshot
     UNION ALL
     SELECT *
     FROM client_loan_snapshot) cs
WHERE event = 'INTEREST'
GROUP BY DATE_TRUNC('MONTH', date)
ORDER BY DATE_TRUNC('MONTH', date);

SELECT *
FROM (
     SELECT delta AS invest_delta, balance AS invest_balance, date, event, id, client_id
     FROM client_invest_snapshot
     UNION ALL
     SELECT delta AS loan_delta, balance AS loan_balance, date, event, id, client_id
     FROM client_loan_snapshot) q
WHERE client_id = 1
ORDER BY date, event;

SELECT *
FROM client
WHERE balance_amount != 0;