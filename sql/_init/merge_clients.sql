DO
$$
    DECLARE
        old_client_id  BIGINT := (271);
        main_client_id BIGINT := (252);

    BEGIN
        UPDATE loan
        SET client_id = main_client_id
        WHERE client_id = old_client_id;
        UPDATE loan_history
        SET client_id = main_client_id
        WHERE client_id = old_client_id;

        UPDATE investment
        SET client_id = main_client_id
        WHERE client_id = old_client_id;
        UPDATE investment_history
        SET client_id = main_client_id
        WHERE client_id = old_client_id;

        UPDATE adjust_balance
        SET client_id = main_client_id
        WHERE client_id = old_client_id;
        UPDATE adjust_balance_history
        SET client_id = main_client_id
        WHERE client_id = old_client_id;

        UPDATE withdrawal
        SET client_id = main_client_id
        WHERE client_id = old_client_id;
        UPDATE withdrawal_history
        SET client_id = main_client_id
        WHERE client_id = old_client_id;
    END
$$;


DELETE
FROM client
WHERE id IN (118, 134, 151, 142, 192, 307, 271);

DELETE
FROM client_invest_snapshot;
DELETE
FROM client_loan_snapshot;


SELECT *
FROM client;

