-- All snapshots in date order
SELECT COALESCE(client.display_name, client.minecraft_username, client.discord_username) username,
       event,
       invest_balance / 64 / 64.0                                                        invest_balance_le,
       invest_delta / 64 / 64.0                                                          invest_delta_le,
       loan_balance / 64 / 64.0                                                          loan_balance_le,
       loan_delta / 64 / 64.0                                                            loan_delta_le,
       date,
       client.id
FROM client_snapshot
         LEFT JOIN client ON client_id = client.id
ORDER BY date;


SELECT COUNT(*),
       client_id,
       COALESCE(client.display_name, client.minecraft_username, client.discord_username) username
FROM client_snapshot
         LEFT JOIN client ON client_id = client.id
GROUP BY client_id, display_name, minecraft_username, discord_username;

SELECT client.minecraft_username,
       event,
       invest_balance / 64 / 64.0 invest_balance_le,
       invest_delta / 64 / 64.0   invest_delta_le,
       loan_balance / 64 / 64.0   loan_balance_le,
       loan_delta / 64 / 64.0     loan_delta_le,
       date
FROM client_snapshot
         LEFT JOIN client ON client_id = client.id
WHERE client_id = 255
ORDER BY client.minecraft_username, date;

SELECT *
FROM client;