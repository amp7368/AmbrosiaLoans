DELETE
FROM staff
WHERE client_id IS NOT NULL
  AND client_id NOT IN (100, 101);