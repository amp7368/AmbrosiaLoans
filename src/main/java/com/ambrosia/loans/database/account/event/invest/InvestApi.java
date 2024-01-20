package com.ambrosia.loans.database.account.event.invest;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class InvestApi {

    public static DInvest createInvestment(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return createInvestEvent(client, date, conductor, emeralds, AccountEventType.INVEST);
    }

    public static DInvest createWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        BalanceWithInterest balance = client.getBalanceWithInterest(Instant.now());
        if (balance.total() < emeralds.amount()) {
            String msg = "Not enough emeralds! Tried withdrawing %s from %s investment".formatted(emeralds, balance.total());
            throw new IllegalStateException(msg);
        }
        return createInvestEvent(client, date, conductor, emeralds.negative(), AccountEventType.WITHDRAWAL);
    }

    @NotNull
    private static DInvest createInvestEvent(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds,
        AccountEventType type) {

        DInvest investment = new DInvest(client, date, conductor, emeralds.amount(), type);
        try (Transaction transaction = DB.beginTransaction()) {
            investment.save(transaction);
            client.updateBalance(emeralds.amount(), date, type, transaction);
            transaction.commit();
        }
        return investment;
    }

}
