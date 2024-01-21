SELECT client.display_name,
       event,
       account_balance / 64 / 64.0 balance_le,
       account_delta / 64 / 64.0   delta_le,
       date,
       client.id
FROM account_sim_snapshot
         LEFT JOIN client ON client_id = client.id
ORDER BY date;

SELECT COUNT(*), client_id, display_name
FROM account_sim_snapshot
         LEFT JOIN client ON client_id = client.id
GROUP BY client_id, display_name;

SELECT client.display_name,
       event,
       account_balance / 64 / 64.0 balance_le,
       account_delta / 64 / 64.0   delta_le,
       date
FROM account_sim_snapshot
         LEFT JOIN client ON client_id = client.id
WHERE client.display_name = 'ClientLoanB'
ORDER BY date;