SELECT TO_CHAR(profits.ptimespan, '''YYMM') period,
       loans.active_loans,
       total_investors,
       CONCAT(rate_of_return, '%')          returns,
       CONCAT(ROUND(AVG(rate_of_return)
                    OVER (ORDER BY ptimespan ROWS BETWEEN 11 PRECEDING AND CURRENT ROW),
                    2), '%')                returns_moving_avg,
       total_profits
FROM (
     SELECT ROUND(SUM(delta) / 4096 / 64, 2)          total_profits,
            ROUND(MIN(balance) / 4096 / 64, 2)        total_investors,
            DATE_TRUNC('MONTH', rate.date)            ptimespan,
            ROUND(SUM(delta) / MIN(balance) * 100, 2) rate_of_return
     FROM (
          SELECT SUM(invest_delta)       delta,
                 SUM(invest_balance) + 1 balance,
                 date
          FROM client_snapshot
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
                                   '2024-02-01', '1 month'::INTERVAL) date
                       INNER JOIN loan ON loan.start_date <= date + '1 month'::INTERVAL AND
                                          COALESCE(loan.end_date, NOW()) > date
              GROUP BY date
              ORDER BY date DESC) loans
              ON profits.ptimespan = loans.date
ORDER BY profits.ptimespan DESC;

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

