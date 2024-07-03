SELECT *
FROM client;

SELECT *
FROM loan
ORDER BY client_id, start_date;

SELECT loan_payment.*, loan.*
FROM loan
         LEFT JOIN loan_payment ON loan.id = loan_payment.loan_id
ORDER BY loan.client_id, loan_payment.date;

SELECT *
FROM client_invest_snapshot
ORDER BY date;

SELECT *
FROM client_loan_snapshot
ORDER BY date DESC;

SELECT *
FROM alter_change;

SELECT *
FROM alter_change_undo_history;

SELECT *
FROM alter_create;

SELECT *
FROM alter_create_undo_history;

SELECT *
FROM loan_payment;
