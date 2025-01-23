SELECT SUM(balance) / 4096. / 64. AS balance
FROM (SELECT (SUM(initial_amount) -
              (SELECT COALESCE(SUM(amount), 0)
               FROM loan_payment
               WHERE loan_id = loan.id)) balance
      FROM loan
      WHERE (loan.status = 'DEFAULTED') = :isDefaulted
      GROUP BY loan.id) q
WHERE balance > 0;


