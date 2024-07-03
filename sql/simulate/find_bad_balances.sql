SELECT DISTINCT c.id
FROM client c
WHERE (
      SELECT balance
      FROM client_invest_snapshot
      WHERE c.id = client_id
      ORDER BY date DESC, event DESC
      LIMIT 1) < 0
   OR (
      SELECT balance
      FROM client_loan_snapshot
      WHERE c.id = client_id
      ORDER BY date DESC, event DESC
      LIMIT 1) > 0;

SELECT DISTINCT c.id
FROM client c
WHERE (
      SELECT balance
      FROM client_invest_snapshot
      WHERE c.id = client_id
      ORDER BY date DESC, event DESC
      LIMIT 1) BETWEEN 1 AND 4096
   OR (
      SELECT balance
      FROM client_loan_snapshot
      WHERE c.id = client_id
      ORDER BY date DESC, event DESC
      LIMIT 1) BETWEEN -1 AND -4096;

SELECT *
FROM adjust_balance
WHERE client_id = 296;

SELECT *
FROM adjust_loan
WHERE loan_id = 272
ORDER BY date DESC;

SELECT *
FROM loan
WHERE id = 293