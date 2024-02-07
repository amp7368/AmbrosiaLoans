SELECT *
FROM loan
         LEFT JOIN loan_section ON loan.id = loan_section.loan_id;

SELECT *
FROM loan_section;


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
FROM loan_payment
WHERE loan_id = 102;

SELECT *
FROM client
ORDER BY id;


SELECT MAX(id) + 1
FROM api_version;
ALTER SEQUENCE api_version_id_seq RESTART WITH 3;

SELECT MAX(id) + 1
FROM client;
ALTER SEQUENCE client_id_seq RESTART WITH 293;

SELECT MAX(id) + 1
FROM staff;
ALTER SEQUENCE staff_id_seq RESTART WITH 11;

SELECT MAX(id) + 1
FROM collateral;
ALTER SEQUENCE collateral_id_seq RESTART WITH 324;

SELECT MAX(id) + 1
FROM loan;
ALTER SEQUENCE loan_id_seq RESTART WITH 272
