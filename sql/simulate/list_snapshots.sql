-- All snapshots in date order
SELECT COALESCE(client.display_name, client.minecraft_username, client.discord_username) username,
       COALESCE(cl.event, ci.event)                                                      event,
       ci.balance / 64.0 / 64                                                            invest_balance_le,
       ci.delta / 64.0 / 64                                                              invest_delta_le,
       cl.balance / 64.0 / 64                                                            loan_balance_le,
       cl.delta / 64.0 / 64                                                              loan_delta_le,
       COALESCE(cl.date, ci.date)                                                        date,
       client.id
FROM client
         LEFT JOIN client_invest_snapshot ci ON ci.client_id = client.id
         LEFT JOIN client_loan_snapshot cl ON cl.client_id = client.id
WHERE client.id = (
                  SELECT client_id
                  FROM loan
                  WHERE loan.id = 102)
ORDER BY date;


SELECT COUNT(*),
       client_id,
       COALESCE(client.display_name, client.minecraft_username, client.discord_username) username
FROM client_snapshot
         LEFT JOIN client ON client_id = client.id
GROUP BY client_id, display_name, minecraft_username, discord_username;

SELECT ROUND(invest_balance / 64.0 / 4096.0, 2) invest_balance_le,
       ROUND(invest_delta / 64.0 / 4096.0, 2)   invest_delta_le,
       ROUND(loan_balance / 64.0 / 4096.0, 2)   loan_balance_le,
       ROUND(loan_delta / 64.0 / 4096.0, 2)     loan_delta_le,
       event,
       date
FROM client_snapshot
         LEFT JOIN client ON client_id = client.id
WHERE client_id = 139
ORDER BY client.minecraft_username, date;

SELECT *
FROM client;


