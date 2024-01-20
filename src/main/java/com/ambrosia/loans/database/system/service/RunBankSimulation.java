package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.balance.query.QDAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.base.IAccountChange;
import com.ambrosia.loans.database.account.event.invest.DInvest;
import com.ambrosia.loans.database.account.event.invest.query.QDInvest;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.loan.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.bank.BankApi;
import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import io.ebean.CallableSql;
import io.ebean.DB;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RunBankSimulation {

    private static final CallableSql UPDATE_SIM_WITH_SNAPSHOT_QUERY = DB.createCallableSql("""
        UPDATE client c
        SET balance_amount       = COALESCE(q.account_balance, 0),   -- account_balance might be null
            balance_last_updated = COALESCE(q.date, TO_TIMESTAMP(0)) -- date might be null
        FROM (
             SELECT DISTINCT ON (c.id) c.id,
                                       ss.account_balance,
                                       ss.date
             FROM client c
                      LEFT JOIN account_sim_snapshot ss ON c.id = ss.client_id
             ORDER BY c.id,
                      ss.date DESC) AS q
        WHERE c.id = q.id;
        """);

    public static void simulateFromDate(Instant simulateStartDate) {
        resetSimulationFromDate(simulateStartDate);
        List<DLoanPayment> loanPayments = findLoanPayments(simulateStartDate);
        List<IAccountChange> accountChanges = findAccountChanges(simulateStartDate);

        int index = 0;
        for (DLoanPayment loanPayment : loanPayments) {
            Instant currentDate = loanPayment.getDate();
            index = doRelevantAccountChange(accountChanges, index, currentDate);
            loanPayment.updateSimulation();

            // investors
            List<DClient> investors = findAllInvestors();
            BigDecimal totalInvested = calcInvestorsAmount(investors, currentDate);

            // divide payment to investors
            BigDecimal amountToInvestors = BigDecimal.valueOf(loanPayment.getAmount())
                .multiply(Bank.INVESTOR_SHARE, MathContext.DECIMAL128);
            long amountGiven = giveToInvestors(loanPayment, investors, amountToInvestors, totalInvested);

            BankApi.updateBankBalance(loanPayment.getAmount() - amountGiven, currentDate, AccountEventType.PROFIT);
        }
        for (int size = accountChanges.size(); index < size; index++) {
            accountChanges.get(index).updateSimulation();
        }
    }

    private static long giveToInvestors(DLoanPayment loanPayment, List<DClient> investors, BigDecimal amountToInvestors,
        BigDecimal totalInvested) {
        long amountGiven = 0;
        Instant currentTime = loanPayment.getDate();
        for (DClient investor : investors) {
            long investorBalance = investor.getBalanceWithInterest(currentTime).total();
            long amountToInvestor = BigDecimal.valueOf(investorBalance)
                .multiply(amountToInvestors)
                .divide(totalInvested, RoundingMode.FLOOR)
                .longValue();
            amountGiven += amountToInvestor;
            investor.updateBalance(amountToInvestor, currentTime, AccountEventType.PROFIT);
        }
        return amountGiven;
    }

    @NotNull
    private static BigDecimal calcInvestorsAmount(List<DClient> investors, Instant currentTime) {
        return BigDecimal.valueOf(investors.stream()
            .map(c -> c.getBalanceWithInterest(currentTime))
            .mapToLong(BalanceWithInterest::total)
            .sum());
    }

    private static int doRelevantAccountChange(List<IAccountChange> accountChanges, int index, Instant loanDate) {
        for (int size = accountChanges.size(); index < size; index++) {
            IAccountChange accountChange = accountChanges.get(index);
            accountChange.getClient().getInterest(loanDate);

            if (accountChange.getDate().isAfter(loanDate))
                break;
            accountChange.updateSimulation();
        }
        return index;
    }

    private static List<DClient> findAllInvestors() {
        return new QDClient().where()
            .balance.amount.gt(0)
            .findList();
    }


    private static void resetSimulationFromDate(Instant fromDateInstant) {
        Timestamp fromDate = Timestamp.from(fromDateInstant);
        new QDAccountSnapshot().where()
            .date.greaterOrEqualTo(fromDate)
            .delete();
        new QDBankSnapshot().where()
            .date.greaterOrEqualTo(fromDate)
            .delete();

        DB.getDefault().execute(UPDATE_SIM_WITH_SNAPSHOT_QUERY);
    }

    private static List<IAccountChange> findAccountChanges(Instant fromDateInstant) {
        Timestamp fromDate = Timestamp.from(fromDateInstant);
        List<DInvest> investments = findInvestmentsAfter(fromDate);
        List<DLoan> loans = findLoansAfter(fromDate);

        List<IAccountChange> changes = new ArrayList<>();
        changes.addAll(investments);
        changes.addAll(loans);

        changes.sort(Comparator.comparing(IAccountChange::getDate));
        return changes;
    }


    private static List<DInvest> findInvestmentsAfter(Timestamp fromDate) {
        return new QDInvest().where()
            .or()
            .date.greaterOrEqualTo(fromDate)
            .endOr()
            .orderBy("date")
            .findList();
    }

    private static List<DLoan> findLoansAfter(Timestamp fromDate) {
        return new QDLoan().where()
            .or()
            .startDate.greaterOrEqualTo(fromDate)
            .endOr()
            .orderBy("startDate")
            .findList();
    }

    private static List<DLoanPayment> findLoanPayments(Instant fromDateInstant) {
        Timestamp fromDate = Timestamp.from(fromDateInstant);
        List<DLoanPayment> loans = new QDLoanPayment().where()
            .or()
            .date.greaterOrEqualTo(fromDate)
            .endOr()
            .orderBy("date")
            .findList();
        loans.sort(Comparator.comparing(DLoanPayment::getDate));
        return loans;
    }

    private static class LoanInterest {

        private final DLoan loan;

        public LoanInterest(DLoan loan) {
            this.loan = loan;
        }
    }
}
