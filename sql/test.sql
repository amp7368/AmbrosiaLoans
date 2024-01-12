SELECT *
FROM loan
         LEFT JOIN loan_section ON loan.id = loan_section.loan_id;

SELECT *
FROM loan_section;

DROP SCHEMA public CASCADE;


SELECT *
FROM loan;
SELECT p.amount, l.initial_amount, l.id
FROM loan_payment p
         LEFT JOIN loan l ON p.loan_id = l.id;
SELECT *
FROM loan_payment;

SELECT client.id, log.id, sim.*
FROM account_sim_snapshot sim
         LEFT JOIN client ON client.account_simulation_id = account_id
         LEFT JOIN client_account_log log ON client.account_log_id = log.id
ORDER BY account_id, date;

SELECT *
FROM loan
WHERE loan.account_id = '5f9d08ca-c2b3-4a74-a329-43fb766042ad'
