SELECT *
FROM client c
         RIGHT JOIN (SELECT DISTINCT c.id
                     FROM client c
                     WHERE (SELECT balance
                            FROM client_invest_snapshot
                            WHERE c.id = client_id
                            ORDER BY date DESC, event DESC
                            LIMIT 1) < 0
                        OR (SELECT balance
                            FROM client_loan_snapshot
                            WHERE c.id = client_id
                            ORDER BY date DESC, event DESC
                            LIMIT 1) > 0) q ON c.id = q.id
ORDER BY c.balance_invest_last_updated DESC;

SELECT c.*
FROM client c
         RIGHT JOIN(SELECT DISTINCT c.id
                    FROM client c
                    WHERE (SELECT balance
                           FROM client_invest_snapshot
                           WHERE c.id = client_id
                           ORDER BY date DESC, event DESC
                           LIMIT 1) BETWEEN 1 AND 4096 * 30
                       OR (SELECT balance
                           FROM client_loan_snapshot
                           WHERE c.id = client_id
                           ORDER BY date DESC, event DESC
                           LIMIT 1) BETWEEN -1 AND -4096 * 30) q ON c.id = q.id;
