-- sum bank profits
SELECT balance / 4096.0 / 64.0 bank_profits, date
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;
