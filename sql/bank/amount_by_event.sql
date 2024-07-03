-- sum events
SELECT cs.event                    event_type,
       SUM(cs.delta) / 4096 / 64.0 total_stx
FROM client c
         LEFT JOIN (
                   SELECT *
                   FROM client_invest_snapshot
                   UNION ALL
                   SELECT *
                   FROM client_loan_snapshot) cs ON c.id = cs.client_id
GROUP BY event_type
ORDER BY event_type;