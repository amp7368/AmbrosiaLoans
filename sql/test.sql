SELECT *
FROM message_id
WHERE message_id = 1344446217601552435;

SELECT *
FROM message_client
WHERE id = '24044280-0b73-4816-8ce1-6c14227dd1d9';

SELECT rate, amount
FROM (SELECT MAX(rate) rate, MAX(loan.initial_amount) / 4096 / 64 amount
      FROM loan_section
               LEFT JOIN loan ON loan_section.loan_id = loan.id
      WHERE loan_section.rate != 0
      GROUP BY loan.id) q;



SELECT COUNT(*), amount = 0 AS iszero
FROM adjust_loan
GROUP BY amount = 0;


SELECT *
FROM alter_change change
         LEFT JOIN alter_create creat ON change.entity_id = creat.id
WHERE type = 'LOAN_DEFAULTED'
  AND creat.id = 224;


SELECT DISTINCT (entity_type), id, created
FROM api_version
ORDER BY created DESC;


SELECT *
FROM message_client
ORDER BY date_created DESC;


SELECT *
FROM bot_blocked_timespan
ORDER BY last_checked_at DESC;