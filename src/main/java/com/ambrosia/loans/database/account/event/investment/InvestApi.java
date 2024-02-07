package com.ambrosia.loans.database.account.event.investment;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.event.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.event.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.event.base.AccountEvent;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public class InvestApi {

    public static DInvestment createInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return (DInvestment) createInvestEvent(client, date, conductor, emeralds, AccountEventType.INVEST);
    }

    public static DWithdrawal createWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        Emeralds balance = client.getInvestBalance(date);
        if (balance.amount() < emeralds.amount()) {
            String msg = "Not enough emeralds! Tried withdrawing %s from %s investment".formatted(emeralds, balance);
            DatabaseModule.get().logger().error(msg);
        }
        return (DWithdrawal) createInvestEvent(client, date, conductor, emeralds.negative(), AccountEventType.WITHDRAWAL);
    }

    private static AccountEvent createInvestEvent(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds,
        AccountEventType type) {
        AccountEvent investment;
        if (type == AccountEventType.INVEST)
            investment = new DInvestment(client, date, conductor, emeralds, type);
        else
            investment = new DWithdrawal(client, date, conductor, emeralds, type);

        try (Transaction transaction = DB.beginTransaction()) {
            client.updateBalance(emeralds.amount(), date, type, transaction);
            investment.save(transaction);
            transaction.commit();
        }
        return investment;
    }

    public static AccountEvent createInvestLike(BaseActiveRequestInvest<?> request) throws InvalidStaffConductorException {
        AccountEvent event;
        if (request.getEventType() == AccountEventType.INVEST) {
            DInvestment investment = new DInvestment(request, Instant.now());
            event = investment;
            event.getClient().addInvestment(investment);
        } else {
            DWithdrawal withdrawal = new DWithdrawal(request, Instant.now());
            event = withdrawal;
            event.getClient().addWithdrawal(withdrawal);
        }
        event.save();

        event.refresh();
        event.getClient().refresh();

        RunBankSimulation.simulate(event.getDate());
        return event;
    }

    public static void createAdjustment(Emeralds difference, DClient client, Instant date, boolean updateBalance) {
        createAdjustment(null, difference, client, date, updateBalance);
    }

    public static void createAdjustment(@Nullable DLoan loan, Emeralds difference, DClient client, Instant date,
        boolean updateBalance) {
        try (Transaction transaction = DB.beginTransaction()) {
            createAdjustment(loan, difference, client, date, updateBalance, transaction);
            transaction.commit();
        }
    }

    public static void createAdjustment(@Nullable DLoan loan, Emeralds difference, DClient client, Instant date,
        boolean updateBalance, Transaction transaction) {
        if (difference.isZero()) return;

        AccountEventType type;
        if (loan != null) type = AccountEventType.ADJUST_LOAN;
        else if (difference.isPositive()) type = AccountEventType.ADJUST_UP;
        else type = AccountEventType.ADJUST_DOWN;

        if (loan == null) {
            DAdjustBalance adjustment = new DAdjustBalance(client, date, DStaffConductor.MIGRATION, difference, type);
            adjustment.save(transaction);
        } else {
            DAdjustLoan adjustment = new DAdjustLoan(loan, date, DStaffConductor.MIGRATION, difference, type);
            adjustment.save(transaction);
            loan.refresh();
        }
        if (updateBalance)
            client.updateBalance(difference.amount(), date, type, transaction);
    }


    public interface InvestQueryApi {

        static BigDecimal getInvestorStake(DClient investor) {
            Emeralds balance = investor.getInvestBalance(Instant.now());
            if (balance.isNegative()) return BigDecimal.ZERO;
            Emeralds totalInvested = new QDClient().where()
                .where().balance.investAmount.gt(0)
                .findStream()
                .map(c -> c.getInvestBalance(Instant.now()))
                .reduce(Emeralds.zero(), Emeralds::add);

            BigDecimal bigTotalInvested = totalInvested.toBigDecimal();
            return balance.toBigDecimal()
                .divide(bigTotalInvested, MathContext.DECIMAL128);
        }
    }
}
