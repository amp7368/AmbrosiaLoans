SELECT MAX(id) + 1
FROM api_version;
ALTER SEQUENCE api_version_id_seq RESTART WITH 3;

SELECT MAX(id) + 1
FROM client;
ALTER SEQUENCE client_id_seq RESTART WITH 293;

SELECT MAX(id) + 1
FROM staff;
ALTER SEQUENCE staff_id_seq RESTART WITH 101;

SELECT MAX(id) + 1
FROM collateral;
ALTER SEQUENCE collateral_id_seq RESTART WITH 970;

SELECT MAX(id) + 1
FROM loan;
ALTER SEQUENCE loan_id_seq RESTART WITH 272
