UPDATE collateral
SET status = returned;

ALTER TABLE message_client
    ADD COLUMN date DATE;
