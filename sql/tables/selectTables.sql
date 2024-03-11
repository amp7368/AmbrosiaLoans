SELECT *
FROM client;

SELECT *
FROM loan
ORDER BY client_id, start_date;

SELECT *
FROM client_snapshot
ORDER BY date;

SELECT *
FROM alter_change;

SELECT *
FROM alter_change_undo_history;

SELECT *
FROM alter_create;

SELECT *
FROM alter_create_undo_history;

SELECT *
FROM loan_section
WHERE loan_id = 274