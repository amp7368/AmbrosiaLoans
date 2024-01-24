package com.ambrosia.loans.database.account.event.invest;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.query.QDClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestment;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class InvestApi {

    public static DInvest createInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return createInvestEvent(client, date, conductor, emeralds, AccountEventType.INVEST);
    }

    public static DInvest createWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        Emeralds balance = client.getBalance(date);
        if (balance.amount() < emeralds.amount()) {
            String msg = "Not enough emeralds! Tried withdrawing %s from %s investment".formatted(emeralds, balance);
            throw new IllegalStateException(msg);
        }
        return createInvestEvent(client, date, conductor, emeralds.negative(), AccountEventType.WITHDRAWAL);
    }

    @NotNull
    private static DInvest createInvestEvent(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds,
        AccountEventType type) {

        DInvest investment = new DInvest(client, date, conductor, emeralds, type);
        try (Transaction transaction = DB.beginTransaction()) {
            client.updateBalance(emeralds.amount(), date, type, transaction);
            investment.save(transaction);
            transaction.commit();
        }
        return investment;
    }

    public static DInvest createInvestment(ActiveRequestInvestment request) throws InvalidStaffConductorException {
        DInvest investment = new DInvest(request);

        investment.save();

        investment.refresh();
        investment.getClient().refresh();

        RunBankSimulation.simulateFromDate(request.getTimestamp());
        return investment;
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
