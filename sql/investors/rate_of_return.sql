SELECT TO_CHAR(loans.date, 'MM/YYYY')                    period,
       CONCAT(COALESCE(ROUND(total_profits * 100 / total_invested_stx, 2), 0),
              '%')                                       "Investor Return",
       CONCAT(ROUND(100.0 / total_invested_stx, 3), '%') "Investor Stake of 1 STX",
       ROUND(total_invested_stx / 100.0, 3)              "STX for 1% Investor Stake",
       ROUND(total_invested_stx, 2)                      "Ambrosia Investment Pool",
       COALESCE(profits.total_profits, 0) AS             "Investor Profits (STX)"
FROM (SELECT SUM(delta) / 4096.0 / 64.0        total_profits,
             AVG(balance) / 4096.0 / 64        total_invested_stx,
             DATE_TRUNC('MONTH', rate.date)    ptimespan,
             SUM(delta) / MIN(balance) * 100.0 rate_of_return
      FROM (SELECT SUM(delta) delta,
                   SUM(LEAST(
                           balance,
                           CASE
                               WHEN date < '10/01/2024' THEN 1000 * 262144
                               ELSE 45 * 262144
                               END
                       ))     balance,
                   date
            FROM client_invest_snapshot
            WHERE event IN ('PROFIT')
            GROUP BY date) rate
      GROUP BY ptimespan) profits
         JOIN (SELECT ROUND((SUM(payments) - SUM(loan_amount)) / 4096 / 64, 2) interest_payments_stx,
                      DATE_TRUNC('MONTH', end_date)                            loan_timespan
               FROM (SELECT SUM(amount)           payments,
                            MIN(l.initial_amount) loan_amount,
                            l.end_date
                     FROM loan_payment p
                              LEFT JOIN loan l ON l.id = p.loan_id
                     WHERE l.status = 'PAID'
                     GROUP BY l.id, end_date) loans
               GROUP BY loan_timespan) pays ON pays.loan_timespan = profits.ptimespan
         JOIN (SELECT ROUND(SUM(loan.initial_amount) / 4096 / 64, 2) active_loans,
                      date
               FROM GENERATE_SERIES('2022-04-01',
                                    NOW(), '1 month'::INTERVAL) date
                        INNER JOIN loan ON loan.start_date <= date + '1 month'::INTERVAL AND
                                           COALESCE(loan.end_date, NOW()) > date
               GROUP BY date
               ORDER BY date DESC) loans
              ON profits.ptimespan = loans.date
WHERE loans.date >= MAKE_DATE(2024, 1, 1)
ORDER BY loans.date;

SELECT SUM(delta) / 4096                                   AS delta,
       event,
       (SELECT date < '2023-01-10 05:00:03.000000 +00:00') AS is_before
FROM client_invest_snapshot
WHERE client_id = 204
GROUP BY is_before, event
ORDER BY is_before DESC, event;

