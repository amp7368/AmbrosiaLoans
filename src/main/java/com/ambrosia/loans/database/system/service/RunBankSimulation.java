package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.adjust.query.QDAdjustBalance;
import com.ambrosia.loans.database.account.adjust.query.QDAdjustLoan;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.investment.query.QDInvestment;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.DLoanStatus;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.account.loan.query.QDLoan;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.account.query.QDClientInvestSnapshot;
import com.ambrosia.loans.database.account.query.QDClientLoanSnapshot;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.account.withdrawal.query.QDWithdrawal;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;

public class RunBankSimulation {

    private static final CallableSql UPDATE_BALANCE_WITH_SNAPSHOT_QUERY = DB.createCallableSql("""
        UPDATE client c
        SET balance_loan_amount        = COALESCE(loan_balance, 0),             -- account_balance might be null
            balance_loan_last_updated  = COALESCE(loan_date, TO_TIMESTAMP(0)),  -- date might be null
            balance_invest_amount      = COALESCE(invest_balance, 0),           -- account_balance might be null
            balance_invest_last_updated= COALESCE(invest_date, TO_TIMESTAMP(0)) -- date might be null
        FROM (
             SELECT DISTINCT ON (c.id) c.id,
                                       cl.balance loan_balance,
                                       cl.date    loan_date
             FROM client c
                      LEFT JOIN client_loan_snapshot cl ON c.id = cl.client_id
             ORDER BY c.id,
                      cl.date DESC,
                      cl.event DESC) AS q1
                 LEFT JOIN
             (
             SELECT DISTINCT ON (c.id) c.id,
                                       ci.balance invest_balance,
                                       ci.date    invest_date
             FROM client c
                      LEFT JOIN client_invest_snapshot ci ON c.id = ci.client_id
             ORDER BY c.id,
                      ci.date DESC,
                      ci.event DESC) q2 ON q1.id = q2.id
        WHERE c.id = q1.id;
        """);
    private static final SqlUpdate RESET_PAID_MARKERS = DB.sqlUpdate("""
        UPDATE loan l
        SET end_date = NULL,
            status   = '%s'
        WHERE status = '%s'
          AND end_date >= :from_date;""".formatted(
        DLoanStatus.ACTIVE,
        DLoanStatus.PAID));

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final List<Runnable> simulations = new ArrayList<>();
    private static boolean isRunning = false;

    public static void simulateAsync(Instant simulateStartDate) {
        synchronized (simulations) {
            simulations.add(() -> simulate(simulateStartDate));
            if (!isRunning) checkSimulationQueue();
        }
    }

    private static void checkSimulationQueue() {
        synchronized (simulations) {
            isRunning = !simulations.isEmpty();
            if (!isRunning) return;
            Runnable run = simulations.remove(0);
            executor.submit(run);
        }
    }

    public static void simulate(Instant simulateStartDate) {
        simulate(simulateStartDate, SimulationOptions.options());
    }

    public static void simulate(Instant simulateStartDate, SimulationOptions options) {
        resetSimulationFromDate(simulateStartDate);
        DatabaseModule.get().logger().info("Starting simulation...");
        List<DLoanPayment> loanPayments = findLoanPayments(simulateStartDate, options);
        List<IAccountChange> accountChanges = findAccountChanges(simulateStartDate, options);

        int index = 0;
        for (DLoanPayment loanPayment : loanPayments) {
            index = simulatePayment(loanPayment, index, accountChanges);
        }
        doRelevantAccountChange(accountChanges, index, options.getEndDate(), false);

        for (DLoan loan : LoanQueryApi.findAllLoans()) {
            loan.checkIsFrozen(true);
        }
        DatabaseModule.get().logger().info("Finished running simulation!");
        checkSimulationQueue();
    }

    private static int simulatePayment(DLoanPayment loanPayment, int index, List<IAccountChange> accountChanges) {
        Instant currentDate = loanPayment.getDate();
        index = doRelevantAccountChange(accountChanges, index, currentDate, true);
        loanPayment.updateSimulation();

        // investors
        List<DClient> investors = findAllInvestors(loanPayment.getLoan().getClient().getId());

        // divide payment to investors
        BigDecimal profits = calcProfits(loanPayment).toBigDecimal();
        BigDecimal amountToInvestors = profits.multiply(Bank.INVESTOR_SHARE, MathContext.DECIMAL128);
        long amountGiven = GiveToInvestors.giveToInvestors(investors, amountToInvestors, currentDate);
        // difference is leftover from rounding errors
        BigDecimal bankProfits = profits.subtract(BigDecimal.valueOf(amountGiven));
        BankApi.updateBankBalance(bankProfits.longValue(), currentDate, AccountEventType.PROFIT);
        return index;
    }

    @NotNull
    public static Emeralds calcProfits(DLoanPayment loanPayment) {
        DLoan loan = loanPayment.getLoan();
        Emeralds pastPayments = loan.getPayments().stream()
            .filter(payment -> payment.getDate().isBefore(loanPayment.getDate()))
            .map(DLoanPayment::getAmount)
            .reduce(Emeralds.zero(), Emeralds::add);
        Emeralds amountPastInitial = pastPayments.add(loan.getInitialAmount().negative());
        if (amountPastInitial.gte(0)) {
            return loanPayment.getAmount();
        }
        Emeralds profits = amountPastInitial.add(loanPayment.getAmount());
        if (profits.isNegative()) return Emeralds.zero();
        return profits;
    }


    private static int doRelevantAccountChange(List<IAccountChange> accountChanges, int index, Instant currentDate,
        boolean isPayment) {
        for (int size = accountChanges.size(); index < size; index++) {
            IAccountChange accountChange = accountChanges.get(index);
            if (accountChange.getDate().isAfter(currentDate))
                break;
            if (isPayment && accountChange.getDate().equals(currentDate)) {
                int compare = AccountEventType.ORDER.compare(accountChange.getEventType(), AccountEventType.PAYMENT);
                if (compare > 0) break;
            }
            accountChange.getClient().refresh();
            accountChange.updateSimulation();
        }
        return index;
    }

    private static List<DClient> findAllInvestors(long notClient) {
        return new QDClient().where()
            .balance.investAmount.gt(0)
            .id.notEqualTo(notClient)
            .findList();
    }


    private static void resetSimulationFromDate(Instant fromDateInstant) {
        Timestamp fromDate = Timestamp.from(fromDateInstant);

        new QDClientInvestSnapshot().where()
            .date.greaterOrEqualTo(fromDate)
            .delete();
        new QDClientLoanSnapshot().where()
            .date.greaterOrEqualTo(fromDate)
            .delete();
        new QDBankSnapshot().where()
            .date.greaterOrEqualTo(fromDate)
            .delete();
        DB.getDefault().execute(RESET_PAID_MARKERS.setParameter("from_date", fromDate));

        DB.getDefault().execute(UPDATE_BALANCE_WITH_SNAPSHOT_QUERY);
    }

    private static List<IAccountChange> findAccountChanges(Instant fromDateInstant, SimulationOptions options) {
        Timestamp fromDate = Timestamp.from(fromDateInstant);
        Timestamp endDate = Timestamp.from(options.getEndDate());
        List<DInvestment> investments = findInvestmentsAfter(fromDate, endDate);
        List<DWithdrawal> withdrawals = findWithdrawalsAfter(fromDate, endDate);
        List<DAdjustLoan> loanAdjustments = findLoanAdjustments(fromDate, endDate);
        List<DAdjustBalance> balanceAdjustments = findBalanceAdjustmentsAfter(fromDate, endDate);
        List<DLoan> loans = findLoansAfter(fromDate, endDate);

        List<IAccountChange> changes = new ArrayList<>();
        changes.addAll(investments);
        changes.addAll(withdrawals);
        changes.addAll(loanAdjustments);
        changes.addAll(balanceAdjustments);
        changes.addAll(loans);

        changes.sort(IAccountChange.ORDER);
        return changes;
    }


    private static List<DInvestment> findInvestmentsAfter(Timestamp fromDate, Timestamp endDate) {
        return new QDInvestment().where()
            .or()
            .date.between(fromDate, endDate)
            .endOr()
            .orderBy("date")
            .findList();
    }

    private static List<DWithdrawal> findWithdrawalsAfter(Timestamp fromDate, Timestamp endDate) {
        return new QDWithdrawal().where()
            .or()
            .date.between(fromDate, endDate)
            .endOr()
            .orderBy("date")
            .findList();
    }

    private static List<DAdjustBalance> findBalanceAdjustmentsAfter(Timestamp fromDate, Timestamp endDate) {
        return new QDAdjustBalance().where()
            .or()
            .date.between(fromDate, endDate)
            .endOr()
            .orderBy("date")
            .findList();
    }

    private static List<DAdjustLoan> findLoanAdjustments(Timestamp fromDate, Timestamp endDate) {
        return new QDAdjustLoan().where()
            .or()
            .date.between(fromDate, endDate)
            .endOr()
            .orderBy("date")
            .findList();
    }

    private static List<DLoan> findLoansAfter(Timestamp fromDate, Timestamp endDate) {
        return new QDLoan().where()
            .or()
            .startDate.between(fromDate, endDate)
            .endOr()
            .orderBy("startDate")
            .findList();
    }

    private static List<DLoanPayment> findLoanPayments(Instant fromDateInstant, SimulationOptions options) {
        Timestamp fromDate = Timestamp.from(fromDateInstant);
        Timestamp endDate = Timestamp.from(options.getEndDate());

        return new QDLoanPayment().where()
            .date.between(fromDate, endDate)
            .orderBy("date")
            .findList();
    }
}
