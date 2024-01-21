package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.balance.query.QDAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.base.IAccountChange;
import com.ambrosia.loans.database.account.event.invest.DInvest;
import com.ambrosia.loans.database.account.event.invest.query.QDInvest;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.account.event.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.loan.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.bank.BankApi;
import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.CallableSql;
import io.ebean.DB;
import io.ebean.SqlUpdate;
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

    private static final CallableSql UPDATE_BALANCE_WITH_SNAPSHOT_QUERY = DB.createCallableSql("""
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
    private static final SqlUpdate RESET_PAID_MARKERS = DB.sqlUpdate("""
        UPDATE loan l
        SET end_date = NULL,
            status   = '%s'
        WHERE status = '%s'
          AND end_date >= :from_date;""".formatted(
        DLoanStatus.ACTIVE.toString(),
        DLoanStatus.PAID.toString()));

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
            BigDecimal totalInvested = calcTotalInvested(investors, currentDate);

            // divide payment to investors
            BigDecimal amountToInvestors = loanPayment.getAmount().toBigDecimal()
                .multiply(Bank.INVESTOR_SHARE, MathContext.DECIMAL128);
            long amountGiven = giveToInvestors(loanPayment, investors, amountToInvestors, totalInvested);

            BankApi.updateBankBalance(loanPayment.getAmount().amount() - amountGiven, currentDate, AccountEventType.PROFIT);
        }
        for (int size = accountChanges.size(); index < size; index++) {
            IAccountChange accountChange = accountChanges.get(index);
            accountChange.getClient().refresh();
            accountChange.updateSimulation();
        }
    }

    private static long giveToInvestors(DLoanPayment loanPayment, List<DClient> investors, BigDecimal amountToInvestors,
        BigDecimal totalInvested) {
        long amountGiven = 0;
        Instant currentTime = loanPayment.getDate();
        for (DClient investor : investors) {
            BigDecimal investorBalance = investor.getBalance(currentTime).toBigDecimal();
            long amountToInvestor = investorBalance
                .multiply(amountToInvestors)
                .divide(totalInvested, RoundingMode.FLOOR)
                .longValue();
            amountGiven += amountToInvestor;
            investor.updateBalance(amountToInvestor, currentTime, AccountEventType.PROFIT);
        }
        return amountGiven;
    }

    @NotNull
    private static BigDecimal calcTotalInvested(List<DClient> investors, Instant currentTime) {
        return investors.stream()
            .map(c -> c.getBalance(currentTime))
            .reduce(Emeralds.of(0), Emeralds::add)
            .toBigDecimal();
    }

    private static int doRelevantAccountChange(List<IAccountChange> accountChanges, int index, Instant currentDate) {
        for (int size = accountChanges.size(); index < size; index++) {
            IAccountChange accountChange = accountChanges.get(index);
            if (accountChange.getDate().isAfter(currentDate))
                break;
            accountChange.getClient().refresh();
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
        DB.getDefault().execute(RESET_PAID_MARKERS.setParameter("from_date", fromDate));

        DB.getDefault().execute(UPDATE_BALANCE_WITH_SNAPSHOT_QUERY);
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
        return new QDLoanPayment().where()
            .or()
            .date.greaterOrEqualTo(fromDate)
            .endOr()
            .orderBy("date")
            .findList();
    }

}
