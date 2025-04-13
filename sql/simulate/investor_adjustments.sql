SELECT ROUND(SUM(delta) / 4096) AS delta, client_id, minecraft_username, balance_invest_amount
FROM client_invest_snapshot
         LEFT JOIN client ON client_invest_snapshot.client_id = client.id
WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
GROUP BY client_id, minecraft_username, balance_invest_amount
HAVING SUM(delta) != 0
ORDER BY delta;

SELECT SUM(delta) / 4096 / 64, event
FROM client_invest_snapshot
GROUP BY event
ORDER BY event;


SELECT TO_CHAR(date, 'MM/DD/YYYY')     AS date,
       ((SELECT SUM(delta)
         FROM (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 AND other.date <= orig.date
               GROUP BY client_id) q
         WHERE delta < 0) +
        (SELECT SUM(delta)
         FROM (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 AND other.date <= orig.date
                 AND other.date >= '06/30/2024'
               GROUP BY client_id) q
         WHERE delta > 0)) / 4096 / 64 AS down,
       ((SELECT SUM(delta)
         FROM (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 AND other.date <= orig.date
               GROUP BY client_id) q
         WHERE delta > 0) +
        (SELECT SUM(delta)
         FROM (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 AND other.date <= orig.date
                 AND other.date >= '06/30/2024'
               GROUP BY client_id) q
         WHERE delta < 0)) / 4096 / 64 AS up,
       (SELECT SUM(delta)
        FROM (SELECT SUM(delta) AS delta
              FROM client_invest_snapshot
              WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                AND client_invest_snapshot.date < orig.date
              GROUP BY client_id) q) / 4096 / 64
                                       AS diff,
       (SELECT SUM(ABS(delta))
        FROM (SELECT SUM(delta) AS delta
              FROM client_invest_snapshot
              WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                AND client_invest_snapshot.date < orig.date
              GROUP BY client_id) q) / 4096 / 64
                                       AS added
FROM client_invest_snapshot orig
WHERE orig.date > '06/30/2024'
GROUP BY orig.date;



SELECT (SELECT SUM(ABS(amount)) / 4096.0 / 64 AS original
        FROM (SELECT SUM(amount) AS amount
              FROM adjust_balance
              WHERE adjust_balance.event_type IN ('ADJUST_UP', 'ADJUST_DOWN')
                AND client_id IN (SELECT id
                                  FROM client
                                  WHERE balance_invest_amount != 0)
              GROUP BY client_id) q) original,
       (SELECT SUM(ABS(amount)) / 4096.0 / 64 AS original
        FROM (SELECT SUM(delta) AS amount
              FROM client_invest_snapshot
              WHERE client_invest_snapshot.event IN ('ADJUST_UP', 'ADJUST_DOWN')
                AND client_id IN (SELECT id
                                  FROM client
                                  WHERE balance_invest_amount != 0)
              GROUP BY client_id) q) current;

