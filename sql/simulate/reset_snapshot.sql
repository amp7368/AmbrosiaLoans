EXPLAIN
UPDATE account_sim cas
SET balance = COALESCE(q.account_balance, 0) -- account_balance might be null
FROM (
     SELECT DISTINCT ON (sim.id) sim.id,
                                 ss.account_balance
     FROM account_sim sim
              LEFT JOIN account_sim_snapshot ss ON sim.id = ss.account_id
     ORDER BY sim.id,
              ss.date DESC) AS q
WHERE cas.id = q.id;

SELECT *
FROM account_sim_snapshot
ORDER BY date;

SELECT *
FROM client_account_log;
SELECT *
FROM client;