package com.ambrosia.loans.database.bank;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.query.QDClientInvestSnapshot;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.SqlQuery;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BankStatisticsQuery {

    private static final SqlQuery ACTIVE_LOAN_BALANCE = sumLoanQuery(false);
    private static final SqlQuery DEFAULTED_LOAN_BALANCE = sumLoanQuery(true);
    private static final SqlQuery INVESTOR_BALANCE_QUERY = investorBalanceQuery();
    private final CachedEmeraldsQueryResult activeLoans = new CachedEmeraldsQueryResult();
    private final CachedEmeraldsQueryResult totalInvested = new CachedEmeraldsQueryResult();
    private final CachedEmeraldsQueryResult defaultedLoans = new CachedEmeraldsQueryResult();
    private final CachedEmeraldsQueryResult investorProfits = new CachedEmeraldsQueryResult();
    private final CachedEmeraldsQueryResult bankBalance = new CachedEmeraldsQueryResult();

    private static SqlQuery investorBalanceQuery() {
        return DB.sqlQuery("""
            SELECT SUM(balance_invest_amount) invested
            FROM client;
            """);
    }

    private static @NotNull SqlQuery sumLoanQuery(boolean isDefaulted) {
        return DB.sqlQuery("""
            SELECT SUM(balance) AS balance
            FROM (SELECT (SUM(initial_amount) -
                          (SELECT COALESCE(SUM(amount), 0)
                           FROM loan_payment
                           WHERE loan_id = loan.id)) balance
                  FROM loan
                  WHERE (loan.status = 'DEFAULTED') = :isDefaulted
                  GROUP BY loan.id) q
            WHERE balance > 0;
            """).setParameter("isDefaulted", isDefaulted);
    }

    public BankStatisticsQuery start() {
        List<Runnable> tasks = List.of(
            this::getActiveLoans,
            this::getTotalInvested,
            this::getDefaultedLoans,
            this::getInvestorProfits,
            this::getBankBalance
        );
        tasks.forEach(Ambrosia.get()::execute);
        return this;
    }

    @NotNull
    public Emeralds getBankBalance() {
        if (bankBalance.start()) return bankBalance.result();
        DBankSnapshot snapshot = BankApi.getLatestSnapshot();
        long balance = snapshot == null ? 0 : snapshot.getBalance();
        return bankBalance.result(balance);
    }

    @NotNull
    public Emeralds getTotalInvested() {
        if (totalInvested.start()) return totalInvested.result();

        Long investment = INVESTOR_BALANCE_QUERY.findOneOrEmpty()
            .map(row -> row.getLong("invested"))
            .orElse(0L);
        return totalInvested.result(investment);
    }

    @NotNull
    public Emeralds getInvestorProfits() {
        if (investorProfits.start()) return investorProfits.result();
        Long profits = new QDClientInvestSnapshot()
            .select("sum(delta)")
            .where().event.eqSubQuery("? :: event_type", AccountEventType.PROFIT.getDBValue())
            .findSingleAttribute();
        return investorProfits.result(profits);
    }

    public Emeralds getDefaultedLoans() {
        if (defaultedLoans.start()) return defaultedLoans.result();
        long defaultedBalance = DEFAULTED_LOAN_BALANCE.findOneOrEmpty()
            .map(row -> row.getLong("balance"))
            .orElse(0L);
        return defaultedLoans.result(defaultedBalance);
    }

    public Emeralds getActiveLoans() {
        if (activeLoans.start()) return activeLoans.result();
        long defaultedBalance = ACTIVE_LOAN_BALANCE.findOneOrEmpty()
            .map(row -> row.getLong("balance"))
            .orElse(0L);
        return activeLoans.result(defaultedBalance);
    }

    private static class CachedEmeraldsQueryResult extends CachedQueryResult<Emeralds> {

        public Emeralds result(Long value) {
            if (value == null)
                return result(Emeralds.zero());
            else return result(Emeralds.of(value));
        }
    }

    private static class CachedQueryResult<T> {

        private final Object sync = new Object();
        private boolean isStarted = false;
        private T result;

        public T result() {
            synchronized (sync) {
                if (result != null) return result;
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        public T result(T newValue) {
            synchronized (sync) {
                result = newValue;
                sync.notifyAll();
            }
            return result;
        }

        /**
         * @return true if the result is being computed
         */
        public boolean start() {
            synchronized (sync) {
                if (isStarted) return true;
                isStarted = true;
                return false;
            }
        }
    }

}
