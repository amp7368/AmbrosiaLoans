package com.ambrosia.loans.database.bank;

import com.ambrosia.loans.bank.Bank;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.base.IAccountChange;
import com.ambrosia.loans.database.log.invest.DInvest;
import com.ambrosia.loans.database.log.invest.query.QDInvest;
import com.ambrosia.loans.database.log.loan.DLoan;
import com.ambrosia.loans.database.log.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.log.loan.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.log.loan.query.QDLoan;
import com.ambrosia.loans.database.simulate.snapshot.query.QDAccountSnapshot;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
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
        SET balance = COALESCE(q.account_balance, 0) -- account_balance might be null
        FROM (
             SELECT DISTINCT ON (c.id) c.id,
                                       ss.account_balance
             FROM client c
                      LEFT JOIN account_sim_snapshot ss ON c.id = ss.client_id
             ORDER BY c.id,
                      ss.date DESC) AS q
        WHERE c.id = q.id;""");

    public static void simulateFromDate(Instant date) {
        Timestamp fromDate = Timestamp.from(date);
        resetSimulationFromDate(fromDate);
        List<DLoanPayment> loans = findLoansSimulation(fromDate);
        List<IAccountChange> accountChanges = findAccountChanges(fromDate);
        int index = 0;
        for (DLoanPayment loan : loans) {
            loan.updateSimulation();
            index = doRelevantAccountChange(accountChanges, index, loan.getDate());
            List<DClient> investors = findAllInvestors();
            BigDecimal totalInvested = reduceToSum(investors);

            long amountToInvestors = BigDecimal.valueOf(loan.getAmount())
                .multiply(Bank.INVESTOR_SHARE, MathContext.DECIMAL64)
                .longValue();
            long amountGiven = giveToInvestors(loan, investors, amountToInvestors, totalInvested);
            for (int size = accountChanges.size(); index < size; index++) {
                accountChanges.get(index).updateSimulation();
            }
            BankApi.updateBankBalance(loan.getAmount() - amountGiven, loan.getDate(), AccountEventType.PAYMENT);
        }
        System.out.println(findAllInvestors().stream().map(DClient::getBalance).toList());
    }

    private static long giveToInvestors(DLoanPayment loan, List<DClient> investors, long amountToInvestors, BigDecimal totalInvested) {
        long amountGiven = 0;
        for (DClient investor : investors) {
            long amountToInvestor = BigDecimal.valueOf(investor.getBalance().amount())
                .multiply(BigDecimal.valueOf(amountToInvestors))
                .divide(totalInvested, RoundingMode.FLOOR)
                .longValue();
            amountGiven += amountToInvestor;
            investor.updateBalance(amountToInvestor, loan.getDate(), AccountEventType.PROFIT);
        }
        return amountGiven;
    }

    @NotNull
    private static BigDecimal reduceToSum(List<DClient> investors) {
        return BigDecimal.valueOf(investors.stream()
            .map(DClient::getBalance)
            .mapToLong(Emeralds::amount)
            .sum());
    }

    private static int doRelevantAccountChange(List<IAccountChange> accountChanges, int index, Instant loanDate) {
        for (int size = accountChanges.size(); index < size; index++) {
            IAccountChange accountChange = accountChanges.get(index);
            if (accountChange.getDate().isAfter(loanDate))
                break;
            accountChange.updateSimulation();
        }
        return index;
    }

    private static List<DClient> findAllInvestors() {
        return new QDClient().where()
            .balance.gt(0)
            .findList();
    }


    private static void resetSimulationFromDate(Timestamp fromDate) {
        new QDAccountSnapshot().where()
            .date.greaterOrEqualTo(fromDate)
            .delete();
        DB.getDefault().execute(UPDATE_SIM_WITH_SNAPSHOT_QUERY);
    }

    private static List<IAccountChange> findAccountChanges(Timestamp fromDate) {
        List<DInvest> investments = findInvestmentsAfter(fromDate);
        List<DLoan> loans = findLoansAfter(fromDate);

        int size = investments.size();
        List<IAccountChange> changes = new ArrayList<>(size);
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

    private static List<DLoanPayment> findLoansSimulation(Timestamp fromDate) {
        List<DLoanPayment> loans = new QDLoanPayment().where()
            .or()
            .date.greaterOrEqualTo(fromDate)
            .endOr()
            .orderBy("date")
            .findList();
        loans.sort(Comparator.comparing(DLoanPayment::getDate));
        return loans;
    }
}
