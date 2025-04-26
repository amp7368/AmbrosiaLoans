-- drop dependencies
ALTER TABLE alter_change
    DROP CONSTRAINT IF EXISTS ck_alter_change_type;
ALTER TABLE collateral
    DROP CONSTRAINT IF EXISTS ck_collateral_returned;
-- apply changes
DROP TRIGGER IF EXISTS collateral_history_upd ON collateral CASCADE;
DROP FUNCTION IF EXISTS collateral_history_version();

DROP VIEW collateral_with_history;
-- apply alter tables
ALTER TABLE collateral
    ALTER COLUMN returned TYPE VARCHAR(9) USING CASE
                                                    WHEN returned THEN 'RETURNED'
                                                    ELSE 'COLLECTED' END;
ALTER TABLE collateral
    ALTER COLUMN returned DROP DEFAULT;
ALTER TABLE collateral
    DROP COLUMN image CASCADE;
ALTER TABLE collateral_history
    DROP COLUMN image CASCADE;
ALTER TABLE collateral
    ALTER COLUMN returned DROP NOT NULL;
ALTER TABLE collateral
    ADD COLUMN returned_date timestamptz;
ALTER TABLE collateral
    ADD COLUMN collection_date timestamptz;
ALTER TABLE collateral
    ADD COLUMN name TEXT;
ALTER TABLE collateral
    ADD COLUMN description VARCHAR(255);
ALTER TABLE collateral_history
    ALTER COLUMN returned TYPE VARCHAR(9) USING CASE
                                                    WHEN returned THEN 'RETURNED'
                                                    ELSE 'COLLECTED' END;
ALTER TABLE collateral_history
    ALTER COLUMN returned DROP NOT NULL;
ALTER TABLE collateral_history
    ADD COLUMN returned_date timestamptz;
ALTER TABLE collateral_history
    ADD COLUMN collection_date timestamptz;
ALTER TABLE collateral_history
    ADD COLUMN name TEXT;
ALTER TABLE collateral_history
    ADD COLUMN description VARCHAR(255);
-- apply post alter
ALTER TABLE alter_change
    ADD CONSTRAINT ck_alter_change_type CHECK ( type IN ('CLIENT_BLACKLISTED', 'LOAN_RATE',
                                                         'LOAN_INITIAL_AMOUNT', 'INVESTMENT_AMOUNT',
                                                         'INVESTMENT_INSTANT', 'LOAN_START_DATE',
                                                         'LOAN_DEFAULTED', 'PAYMENT_AMOUNT',
                                                         'LOAN_FREEZE', 'COLLATERAL_STATUS'));
CREATE VIEW collateral_with_history AS
SELECT *
FROM collateral
UNION ALL
SELECT *
FROM collateral_history;
CREATE OR REPLACE FUNCTION collateral_history_version() RETURNS TRIGGER AS
$$
DECLARE
    lowerts timestamptz;
    upperts timestamptz;
BEGIN
    lowerts = LOWER(old.sys_period);
    upperts = GREATEST(lowerts + '1 microsecond', CURRENT_TIMESTAMP);
    IF (tg_op = 'UPDATE') THEN
        INSERT INTO collateral_history (sys_period, id, loan_id, link, returned_date,
                                        collection_date, name, description, returned, image)
        VALUES (TSTZRANGE(lowerts, upperts), old.id, old.loan_id, old.link, old.returned_date,
                old.collection_date, old.name, old.description, old.returned, old.image);
        new.sys_period = TSTZRANGE(upperts, NULL);
        RETURN new;
    ELSIF (tg_op = 'DELETE') THEN
        INSERT INTO collateral_history (sys_period, id, loan_id, link, returned_date,
                                        collection_date, name, description, returned, image)
        VALUES (TSTZRANGE(lowerts, upperts), old.id, old.loan_id, old.link, old.returned_date,
                old.collection_date, old.name, old.description, old.returned, old.image);
        RETURN old;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER collateral_history_upd
    BEFORE UPDATE OR DELETE
    ON collateral
    FOR EACH ROW
EXECUTE PROCEDURE collateral_history_version();

ALTER TABLE collateral
    ADD CONSTRAINT ck_collateral_returned CHECK ( returned IN ('COLLECTED', 'RETURNED', 'SOLD'));
