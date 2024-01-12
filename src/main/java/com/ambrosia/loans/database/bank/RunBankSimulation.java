package com.ambrosia.loans.database.bank;

import com.ambrosia.loans.bank.Bank;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.base.IAccountChange;
import com.ambrosia.loans.database.log.invest.DInvest;
import com.ambrosia.loans.database.log.invest.query.QDInvest;
import com.ambrosia.loans.database.log.loan.DLoan;
import com.ambrosia.loans.database.log.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.log.loan.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.log.loan.query.QDLoan;
import com.ambrosia.loans.database.simulate.DAccountSimulation;
import com.ambrosia.loans.database.simulate.query.QDAccountSimulation;
import com.ambrosia.loans.database.simulate.snapshot.query.QDAccountSnapshot;
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

public class RunBankSimulation {

    private static final CallableSql UPDATE_SIM_WITH_SNAPSHOT_QUERY = DB.createCallableSql("""
        UPDATE account_sim cas
        SET balance = COALESCE(q.account_balance, 0)
        FROM (
             SELECT DISTINCT ON (sim.id) sim.id,
                                         ss.account_balance
             FROM account_sim sim
                      LEFT JOIN account_sim_snapshot ss ON sim.id = ss.account_id
             ORDER BY sim.id,
                      ss.date DESC) AS q
        WHERE cas.id = q.id;
        """);

    public static void simulateFromDate(Instant date) {
        Timestamp fromDate = Timestamp.from(date);
        resetSimulationFromDate(fromDate);
        List<DLoanPayment> loans = findLoansSimulation(fromDate);
        List<IAccountChange> accountChanges = findAccountChanges(fromDate);
        int index = 0;
        for (DLoanPayment loan : loans) {
            loan.updateSimulation();
            index = doRelaventAccountChange(accountChanges, index, loan.getDate());
            List<DAccountSimulation> investors = findAllInvestors();
            BigDecimal totalInvested = BigDecimal.valueOf(investors.stream()
                .mapToLong(DAccountSimulation::getBalance)
                .sum());

            long amountLeft = BigDecimal.valueOf(loan.getAmount())
                .multiply(Bank.INVESTOR_SHARE, MathContext.DECIMAL64)
                .longValue();
            long amountGiven = 0;
            for (DAccountSimulation investor : investors) {
                long amountToInvestor = BigDecimal.valueOf(investor.getBalance())
                    .multiply(BigDecimal.valueOf(amountLeft))
                    .divide(totalInvested, RoundingMode.FLOOR)
                    .longValue();
                amountLeft -= amountToInvestor;
                amountGiven += amountToInvestor;
                investor.updateBalance(amountToInvestor, loan.getDate());
            }

            BankApi.updateBankBalance(loan.getAmount() - amountGiven, loan.getDate(), AccountEventType.PAYMENT);
        }
        System.out.println(findAllInvestors().stream().map(DAccountSimulation::getBalance).toList());
    }

    private static int doRelaventAccountChange(List<IAccountChange> accountChanges, int index, Instant loanDate) {
        for (int size = accountChanges.size(); index < size; index++) {
            IAccountChange accountChange = accountChanges.get(index);
            if (accountChange.getDate().isAfter(loanDate))
                break;
            accountChange.updateSimulation();
        }
        return index;
    }

    private static List<DAccountSimulation> findAllInvestors() {
        return new QDAccountSimulation().where()
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
