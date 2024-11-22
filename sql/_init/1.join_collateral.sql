UPDATE collateral
SET description = (SELECT STRING_AGG(link, ' ')
                   FROM collateral c
                   WHERE c.loan_id = collateral.loan_id)
FROM (SELECT MIN(id) AS id
      FROM collateral
      GROUP BY loan_id
      ORDER BY loan_id) q
WHERE collateral.id = q.id;

UPDATE collateral
SET collection_date =
        (SELECT loan.start_date FROM loan WHERE loan.id = collateral.loan_id),
    returned_date   =
        (SELECT loan.end_date FROM loan WHERE loan.id = collateral.loan_id),
    returned        = (SELECT CASE WHEN loan.status = 'PAID' THEN 'RETURNED' ELSE 'COLLECTED' END
                       FROM loan
                       WHERE loan.id = collateral.loan_id);

DELETE
FROM collateral
WHERE description IS NULL;

SELECT *
FROM collateral
WHERE id IN (SELECT MIN(id)
             FROM collateral
             GROUP BY loan_id
             ORDER BY MIN(id))
ORDER BY loan_id, id;


SELECT client_id, description
FROM collateral
         LEFT JOIN loan l ON l.id = collateral.loan_id;
