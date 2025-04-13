-- sum bank profits
SELECT balance / 4096.0 / 64.0 bank_profits, date
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;

-- Investor Profits
SELECT SUM(delta) / 4096 / 64 investor_profits_stx, DATE_TRUNC('MONTH', date)
FROM client_invest_snapshot
WHERE event = 'PROFIT'
GROUP BY DATE_TRUNC('MONTH', date)
ORDER BY DATE_TRUNC('MONTH', date);

