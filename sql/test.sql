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
WHERE loan_id = 102

select * from client order by id;


select max(id)+1 from api_version;
alter SEQUENCE api_version_id_seq RESTART WITH 3

select max(id)+1 from client;
alter SEQUENCE client_id_seq RESTART WITH 293;

select max(id)+1 from staff;
alter SEQUENCE staff_id_seq RESTART WITH 11;

select max(id)+1 from collateral;
alter SEQUENCE collateral_id_seq RESTART WITH 324;

select max(id)+1 from loan;
alter SEQUENCE loan_id_seq RESTART WITH 272
