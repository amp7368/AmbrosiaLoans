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

