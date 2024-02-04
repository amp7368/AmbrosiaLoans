-- sum bank profits
SELECT balance / 4096 / 64.0 stx
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;


-- sum loan interest
SELECT event, SUM(invest_delta + loan_delta) / 4096 / 64.0 total_stx
FROM client_snapshot
GROUP BY event
ORDER BY SUM(invest_delta + loan_delta);


SELECT adjust_up.stx, payment.stx + loan.stx profits
FROM (
     SELECT SUM(account_delta) / 4096 / 64.0 stx
     FROM client_snapshot
     WHERE event = 'ADJUST_UP') adjust_up
         LEFT JOIN (
                   SELECT SUM(account_delta) / 4096 / 64.0 stx
                   FROM client_snapshot
                   WHERE event = 'PAYMENT') payment ON TRUE
         LEFT JOIN (
                   SELECT SUM(account_delta) / 4096 / 64.0 stx
                   FROM client_snapshot
                   WHERE event = 'LOAN') loan ON TRUE;

SELECT *
FROM loan
WHERE client_id = 178;

SELECT *
FROM loan_section
WHERE loan_id = 236;

SELECT *
FROM loan_payment
WHERE loan_id = 148