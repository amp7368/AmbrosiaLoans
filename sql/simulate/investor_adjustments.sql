SELECT SUM(delta) AS delta, client_id
FROM client_invest_snapshot
WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
GROUP BY client_id
HAVING SUM(delta) != 0;

SELECT SUM(invest_delta), SUM(loan_delta), event
FROM client_snapshot
WHERE date = '2024-03-13 01:16:46.150156 +00:00'
GROUP BY event;


SELECT COUNT(id), client_id
FROM loan
WHERE client_id = 181
GROUP BY client_id
;

SELECT SUM(invest_delta), SUM(loan_delta), event
FROM client_snapshot
GROUP BY event
ORDER BY event;

SELECT invest_delta, invest_balance, loan_delta, loan_balance, event, date
FROM client_snapshot
WHERE client_id = 117
ORDER BY date, event;

SELECT *
FROM loan
WHERE id = 129;

SELECT *
FROM loan_section
WHERE loan_id = 129;

SELECT *
FROM loan_payment
WHERE loan_id = 129;

SELECT *
FROM client_snapshot
WHERE client_id = 118
ORDER BY date;