-- sum bank profits
SELECT balance / 4096 / 64.0 stx, date
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;


-- sum loan interest
SELECT COALESCE(cis.event, cls.event)                event_type,
       SUM(cis.delta) + SUM(cls.delta) / 4096 / 64.0 total_stx
FROM client c
         LEFT JOIN client_invest_snapshot cis ON c.id = cis.client_id
         LEFT JOIN client_loan_snapshot cls ON c.id = cls.client_id
GROUP BY event_type
ORDER BY SUM(cis.delta + cls.delta);




