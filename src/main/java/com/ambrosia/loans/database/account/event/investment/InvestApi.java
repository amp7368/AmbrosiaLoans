package com.ambrosia.loans.database.account.event.investment;

import com.ambrosia.loans.database.account.event.base.AccountEventInvest;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
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
import java.util.Optional;

public class InvestApi {

    public static DInvestment createInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return (DInvestment) createInvestEvent(client, date, conductor, emeralds, AccountEventType.INVEST);
    }

    public static DWithdrawal createWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        Emeralds balance = client.getBalance(date);
        if (balance.amount() < emeralds.amount()) {
            String msg = "Not enough emeralds! Tried withdrawing %s from %s investment".formatted(emeralds, balance);
            throw new IllegalStateException(msg);
        }
        return (DWithdrawal) createInvestEvent(client, date, conductor, emeralds.negative(), AccountEventType.WITHDRAWAL);
    }

    private static AccountEventInvest createInvestEvent(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds,
        AccountEventType type) {
        AccountEventInvest investment;
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

    public static AccountEventInvest createInvestLike(BaseActiveRequestInvest<?> request) throws InvalidStaffConductorException {
        AccountEventInvest event;
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

        RunBankSimulation.simulateFromDate(event.getDate());
        return event;
    }

    public interface InvestQueryApi {

        static BigDecimal getInvestorStake(DClient investor) {
            Emeralds balance = investor.getBalance(Instant.now());
            if (balance.isNegative()) return BigDecimal.ZERO;
            Optional<Emeralds> totalInvested = new QDClient().where()
                .where().balance.amount.gt(0)
                .findStream()
                .map(c -> c.getBalance(Instant.now()))
                .reduce(Emeralds::add);
            if (totalInvested.isEmpty()) return BigDecimal.ZERO;

            BigDecimal bigTotalInvested = totalInvested.get().toBigDecimal();
            return balance.toBigDecimal()
                .divide(bigTotalInvested, MathContext.DECIMAL128);
        }
    }
}
