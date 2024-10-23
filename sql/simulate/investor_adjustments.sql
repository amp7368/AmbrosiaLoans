SELECT SUM(delta) AS delta, client_id, minecraft_username, balance_invest_amount
FROM client_invest_snapshot
         left join client on client_invest_snapshot.client_id = client.id
WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
GROUP BY client_id, minecraft_username, balance_invest_amount
HAVING SUM(delta) != 0
order by delta;

SELECT SUM(delta) / 4096 / 64, event
FROM client_invest_snapshot
GROUP BY event
ORDER BY event;
