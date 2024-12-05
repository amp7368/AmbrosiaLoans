select round(sum(balance_loan_amount) / 4096. / 64., 2)   active_loan,
       round(sum(balance_invest_amount) / 4096. / 64., 2) invested,
       (SELECT round(balance / 4096. / 64., 2) bank_profits
        FROM bank_snapshot
        ORDER BY date DESC
        LIMIT 1)                                          bank,
       (SELECT round(sum(delta) / 4096. / 64., 2)
        FROM client_invest_snapshot
        WHERE event = 'ADJUST_DOWN')                      adjust_down,
       (SELECT round(sum(delta) / 4096. / 64., 2)
        FROM client_invest_snapshot
        WHERE event = 'ADJUST_UP')                        adjust_up
from client;

