-- sum bank profits
SELECT balance / 4096.0 / 64.0 bank_profits, date
FROM bank_snapshot
ORDER BY date DESC
LIMIT 1;

-- Current Investments & Loans
select sum(balance_invest_amount) / 4096 / 64 invest,
       sum(balance_loan_amount) / 4096 / 64   loan
from client
where balance_invest_amount > 0
   or balance_loan_amount < 0;

-- Investor Profits
select sum(delta) / 4096 / 64
from client_invest_snapshot
where event = 'PROFIT';
