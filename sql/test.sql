SELECT *
FROM loan
         LEFT JOIN loan_section ON loan.id = loan_section.loan_id;

SELECT *
FROM loan_section;

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;