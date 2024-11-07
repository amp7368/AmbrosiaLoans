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


select to_char(date, 'MM/DD/YYYY')     as date,
       ((select sum(delta)
         from (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 and other.date <= orig.date
               GROUP BY client_id) q
         where delta < 0) +
        (select sum(delta)
         from (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 and other.date <= orig.date
                 and other.date >= '06/30/2024'
               GROUP BY client_id) q
         where delta > 0)) / 4096 / 64 as down,
       ((select sum(delta)
         from (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 and other.date <= orig.date
               GROUP BY client_id) q
         where delta > 0) +
        (select sum(delta)
         from (SELECT SUM(delta) AS delta
               FROM client_invest_snapshot other
               WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                 and other.date <= orig.date
                 and other.date >= '06/30/2024'
               GROUP BY client_id) q
         where delta < 0)) / 4096 / 64 as up,
       (select sum(delta)
        from (SELECT SUM(delta) AS delta
              FROM client_invest_snapshot
              WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                and client_invest_snapshot.date < orig.date
              GROUP BY client_id) q) / 4096 / 64
                                       as diff,
       (select sum(abs(delta))
        from (SELECT SUM(delta) AS delta
              FROM client_invest_snapshot
              WHERE event IN ('ADJUST_DOWN', 'ADJUST_UP')
                and client_invest_snapshot.date < orig.date
              GROUP BY client_id) q) / 4096 / 64
                                       as added
from client_invest_snapshot orig
where orig.date > '06/30/2024'
group by orig.date
