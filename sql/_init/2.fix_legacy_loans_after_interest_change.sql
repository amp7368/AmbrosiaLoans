UPDATE loan
SET start_date = '2022-06-17 04:00:01.000000 +00:00'::TIMESTAMP + ('6 HOURS' ::INTERVAL)
WHERE id = 149;
UPDATE loan_section
SET start_date = '2022-06-17 04:00:01.000000 +00:00'::TIMESTAMP + ('6 HOURS' ::INTERVAL)
WHERE id = '2b477434-1cf5-4dae-b7ae-fba1b0e5998f';

UPDATE loan_section
SET end_date = NULL
WHERE id = '121e2b3c-24c5-4b39-be56-898422794392';

UPDATE adjust_loan
SET amount = 133220
WHERE id = 102;

UPDATE adjust_loan
SET amount  = 655,
    loan_id = 346
WHERE id = 101;
