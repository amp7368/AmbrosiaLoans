SELECT client.*
FROM (SELECT client.id,
             MAX(similarity(names.name, 'alloc') /
                 (COALESCE(EXTRACT(
                                   DAYS FROM AGE(NOW(), names.last_used)) / 365,
                           0) + 1)) AS score
      FROM name_history AS names
               LEFT JOIN client ON client.id = names.client_id
      GROUP BY client.id
      ORDER BY score DESC) matches
         LEFT JOIN client ON matches.id = client.id
WHERE score != 0
ORDER BY score DESC,
         UPPER(display_name),
         UPPER(minecraft_username),
         UPPER(discord_username);
