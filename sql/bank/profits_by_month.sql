SELECT DATE_TRUNC('MONTH', date AT TIME ZONE 'UTC') AT TIME ZONE '%s' AS month_date,
       SUM(delta)                                                     AS delta
FROM bank_snapshot
GROUP BY month_date
ORDER BY month_date;
