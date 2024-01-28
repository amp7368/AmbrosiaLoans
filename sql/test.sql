SELECT *
FROM loan
         LEFT JOIN loan_section ON loan.id = loan_section.loan_id;

SELECT *
FROM loan_section;

DROP SCHEMA public CASCADE;


SELECT *
FROM loan;

SELECT *
FROM comment;

SELECT p.amount, l.initial_amount, l.id
FROM loan_payment p
         LEFT JOIN loan l ON p.loan_id = l.id;
SELECT *
FROM loan_payment;

SELECT *
FROM loan
WHERE loan.client_id = '5f9d08ca-c2b3-4a74-a329-43fb766042ad'
