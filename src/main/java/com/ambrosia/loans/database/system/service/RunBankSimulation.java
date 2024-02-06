package com.ambrosia.loans.database.system.service;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.balance.query.QDAccountSnapshot;
import com.ambrosia.loans.database.account.event.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.event.adjust.query.QDAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.query.QDAdjustLoan;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.base.IAccountChange;
import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.investment.query.QDInvestment;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.DLoanStatus;
import com.ambrosia.loans.database.account.event.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.account.event.loan.query.QDLoan;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.account.event.payment.query.QDLoanPayment;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.account.event.withdrawal.query.QDWithdrawal;
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
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RunBankSimulation {

    private static final CallableSql UPDATE_BALANCE_WITH_SNAPSHOT_QUERY = DB.createCallableSql("""
        UPDATE client c
        SET balance_loan_amount   = COALESCE(q.loan_balance, 0),      -- account_balance might be null
            balance_invest_amount = COALESCE(q.invest_balance, 0),    -- account_balance might be null
            balance_last_updated  = COALESCE(q.date, TO_TIMESTAMP(0)) -- date might be null
        FROM (
             SELECT DISTINCT ON (c.id) c.id,
                                       ss.invest_balance,
                                       ss.loan_balance,
                                       ss.date
             FROM client c
                      LEFT JOIN client_snapshot ss ON c.id = ss.client_id
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
        DLoanStatus.ACTIVE,
        DLoanStatus.PAID));

    public static void simulate(Instant simulateStartDate) {
        simulate(simulateStartDate, SimulationOptions.options());
    }

    public static void simulate(Instant simulateStartDate, SimulationOptions options) {
        resetSimulationFromDate(simulateStartDate, options);
        List<DLoanPayment> loanPayments = findLoanPayments(simulateStartDate, options);
        List<IAccountChange> accountChanges = findAccountChanges(simulateStartDate, options);

        int index = 0;
        for (DLoanPayment loanPayment : loanPayments) {
            index = simulatePayment(loanPayment, index, accountChanges);
        }
        doRelevantAccountChange(accountChanges, index, options.getEndDate());

        for (DLoan loan : LoanQueryApi.findAllLoans()) {
            loan.checkIsFrozen(true);
        }
    }

    public static int simulatePayment(DLoanPayment loanPayment, int index, List<IAccountChange> accountChanges) {
        Instant currentDate = loanPayment.getDate();
        index = doRelevantAccountChange(accountChanges, index, currentDate);
        loanPayment.updateSimulation();

        // investors
        List<DClient> investors = findAllInvestors(loanPayment.getLoan().getClient().getId());
        BigDecimal totalInvested = calcTotalInvested(investors, currentDate);

        // divide payment to investors
        BigDecimal profits = calcProfits(loanPayment).toBigDecimal();
        BigDecimal amountToInvestors = profits.multiply(Bank.INVESTOR_SHARE, MathContext.DECIMAL128);
        // difference is leftover from rounding errors
        long amountGiven = giveToInvestors(loanPayment, investors, amountToInvestors, totalInvested);

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

    private static long giveToInvestors(DLoanPayment loanPayment, List<DClient> investors, BigDecimal amountToInvestors,
        BigDecimal totalInvested) {
        long amountGiven = 0;
        Instant currentTime = loanPayment.getDate();
        for (DClient investor : investors) {
            BigDecimal investorBalance = investor.getInvestBalance(currentTime).toBigDecimal();
            long amountToInvestor = investorBalance
                .multiply(amountToInvestors)
                .divide(totalInvested, RoundingMode.FLOOR)
                .longValue();
            if (amountToInvestor == 0) continue;
            amountGiven += amountToInvestor;
            investor.updateBalance(amountToInvestor, currentTime, AccountEventType.PROFIT);
        }
        return amountGiven;
    }

    @NotNull
    private static BigDecimal calcTotalInvested(List<DClient> investors, Instant currentTime) {
        return investors.stream()
            .map(c -> c.getInvestBalance(currentTime))
            .reduce(Emeralds.zero(), Emeralds::add)
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

    private static List<DClient> findAllInvestors(long notClient) {
        return new QDClient().where()
            .balance.investAmount.gt(0)
            .id.notEqualTo(notClient)
            .findList();
    }


    private static void resetSimulationFromDate(Instant fromDateInstant, SimulationOptions options) {
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
