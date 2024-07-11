SELECT TO_CHAR(loans.date, 'MM/YYYY')                    period,
       CONCAT(COALESCE(rate_of_return, 0), '%')          "Return on Investment",
--        loans.active_loans,
       ROUND(total_invested_stx, 2)                      "Total Invested (STX)",
       CONCAT(ROUND(100.0 / total_invested_stx, 3), '%') "Stake of 1 STX",
       COALESCE(profits.total_profits, 0) AS             "Investor Profits (STX)"
FROM (
     SELECT SUM(delta) / 4096.0 / 64.0                  total_profits,
            MIN(balance) / 4096.0 / 64                  total_invested_stx,
            DATE_TRUNC('MONTH', rate.date)              ptimespan,
            ROUND(SUM(delta) / MIN(balance) * 100.0, 2) rate_of_return
     FROM (
          SELECT SUM(delta)       delta,
                 SUM(balance) + 1 balance,
                 date
          FROM client_invest_snapshot
          WHERE event IN ('PROFIT')
          GROUP BY date) rate
     GROUP BY ptimespan) profits
         JOIN (
              SELECT ROUND((SUM(payments) - SUM(loan_amount)) / 4096 / 64, 2) interest_payments_stx,
                     DATE_TRUNC('MONTH', end_date)                            loan_timespan
              FROM (
                   SELECT SUM(amount)           payments,
                          MIN(l.initial_amount) loan_amount,
                          l.end_date
                   FROM loan_payment p
                            LEFT JOIN loan l ON l.id = p.loan_id
                   WHERE l.status = 'PAID'
                   GROUP BY l.id, end_date) loans
              GROUP BY loan_timespan) pays ON pays.loan_timespan = profits.ptimespan
         JOIN (
              SELECT ROUND(SUM(loan.initial_amount) / 4096 / 64, 2) active_loans,
                     date
              FROM GENERATE_SERIES('2022-04-01',
                                   NOW(), '1 month'::INTERVAL) date
                       INNER JOIN loan ON loan.start_date <= date + '1 month'::INTERVAL AND
                                          COALESCE(loan.end_date, NOW()) > date
              GROUP BY date
              ORDER BY date DESC) loans
              ON profits.ptimespan = loans.date
ORDER BY loans.date DESC;

SELECT SUM(invest_delta)                                                             delta,
       MIN(invest_balance) - MIN(invest_delta)                                       balance,
       ROUND(SUM(invest_delta) / (MIN(invest_balance) - MIN(invest_delta)) * 100, 2) rate_of_return,
       DATE_TRUNC('MONTH', date)                                                     timespan
FROM client_snapshot
WHERE client_id = 210
  AND event = 'PROFIT'
GROUP BY timespan
ORDER BY timespan DESC
;



SELECT SUM(invest_delta) / 4096 / 64 adjustments,
       DATE_TRUNC('YEAR', date)      timespan
FROM client_snapshot
WHERE event IN ('ADJUST_DOWN')
GROUP BY timespan
ORDER BY timespan;

