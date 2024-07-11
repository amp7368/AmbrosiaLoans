DROP VIEW collateral_with_history;

ALTER TABLE collateral
    ALTER COLUMN returned TYPE VARCHAR(9)
        USING CASE
                  WHEN returned THEN 'RETURNED'
                  ELSE 'COLLECTED' END,
    DROP COLUMN image;
ALTER TABLE collateral_history
    ALTER COLUMN returned TYPE VARCHAR(9)
        USING CASE
                  WHEN returned THEN 'RETURNED'
                  ELSE 'COLLECTED' END,
    DROP COLUMN image;

CREATE VIEW collateral_with_history AS
SELECT (returned)
FROM collateral_history;

SELECT *
FROM db_migration;
DROP TABLE db_migration;