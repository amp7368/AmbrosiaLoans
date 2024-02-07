-- sum bank profits
SELECT balance / 4096 / 64.0 stx, date
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;


-- sum loan interest
SELECT event, SUM(invest_delta + loan_delta) / 4096 / 64.0 total_stx
FROM client_snapshot
GROUP BY event
ORDER BY SUM(invest_delta + loan_delta);




