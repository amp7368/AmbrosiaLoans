SELECT client.id                      AS                client_id,
       client.discord_username        AS                discord,
       client.minecraft_username      AS                minecraft,
       EXISTS(SELECT *
              FROM loan
              WHERE loan.client_id = client.id
                AND end_date IS NULL) AS                active_loan,
       TO_CHAR(is_bot_blocked_checked_at, 'MM/DD/YYYY') last_checked,
       client_meta.is_bot_blocked     AS                is_bot_blocked
FROM client_meta
         LEFT JOIN client ON client.meta_id = client_meta.id
ORDER BY is_bot_blocked DESC,
         active_loan DESC,
         is_bot_blocked_checked_at DESC;

SELECT client.id                 AS client_id,
       client.discord_username   AS discord,
       client.minecraft_username AS minecraft,
       message_client.message_id,
       acknowledged,
       message_client.date_created,
       message,
       sent_message_obj
FROM message_client
         LEFT JOIN client ON message_client.client_id = client.id
WHERE reason = 'LOAN_REMINDER'
ORDER BY acknowledged DESC,
         message_client.date_created DESC;
