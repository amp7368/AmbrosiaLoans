-- All snapshots in date order
SELECT COALESCE(client.display_name, client.minecraft_username, client.discord_username) username,
       event,
       account_balance / 64 / 64.0                                                       balance_le,
       account_delta / 64 / 64.0                                                         delta_le,
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
       account_balance / 64 / 64.0 balance_le,
       account_delta / 64 / 64.0   delta_le,
       date
FROM client_snapshot
         LEFT JOIN client ON client_id = client.id
ORDER BY client.minecraft_username, date;

SELECT *
FROM client;