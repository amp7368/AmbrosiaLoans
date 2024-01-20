SELECT client.display_name,
       event,
       account_balance / 64 / 64.0 balance_le,
       account_delta / 64 / 64.0   delta_le,
       date,
       client.id
FROM account_sim_snapshot
         LEFT JOIN client ON client_id = client.id
ORDER BY date;

SELECT client.display_name,
       event,
       account_balance / 64 / 64.0 balance_le,
       account_delta / 64 / 64.0   delta_le,
       date
FROM account_sim_snapshot
         LEFT JOIN client ON client_id = client.id
WHERE client.id = :client_id
ORDER BY date;