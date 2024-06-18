SELECT c.*, date, delta / 4096.0 AS delta_le, balance / 4096.0 AS balance_le, event
FROM (
     SELECT id, balance_invest_amount
     FROM client) c
         LEFT JOIN client_invest_snapshot cis ON c.id = cis.client_id
WHERE date > '2024-06-18 00:57:00'
ORDER BY client_id;

SELECT client_id,
       minecraft_username                              username,
       ROUND(SUM(delta) / 4096.0, 2) AS                net_adjustments_le,
       ROUND(MAX(c.balance_invest_amount) / 4096.0, 1) invested_le,
       (
       SELECT CONCAT(ROUND(
                             MAX(c.balance_invest_amount) * 100.0
                                 / SUM(client.balance_invest_amount)
                         , 2), '%')
       FROM client)                                    stake
FROM client_invest_snapshot cis
         LEFT JOIN client c ON cis.client_id = c.id
WHERE event IN ('ADJUST_UP', 'ADJUST_DOWN')
  AND client_id IN (
                   SELECT id
                   FROM client c
                   WHERE c.balance_invest_amount > 0)
GROUP BY client_id, minecraft_username
ORDER BY ABS(SUM(delta)) DESC;

SELECT client_id,
       SUM(delta) / MIN(balance) AS    ratio,
       ROUND(SUM(delta) / 4096.0, 3)   delta_le,
       ROUND(MIN(balance) / 4096.0, 3) balance_le
FROM (
     SELECT id, balance_invest_amount
     FROM client) c
         LEFT JOIN client_invest_snapshot cis ON c.id = cis.client_id
WHERE date > '2024-06-18 00:57:00'
GROUP BY client_id
ORDER BY MIN(balance) DESC;


SELECT SUM(delta) / 4096.0 AS delta_le, SUM(balance) / 4096.0 AS balance_le
FROM (
     SELECT id, balance_invest_amount
     FROM client) c
         LEFT JOIN client_invest_snapshot cis ON c.id = cis.client_id
WHERE date > '2024-06-18 00:57:00';


