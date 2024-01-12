package com.ambrosia.loans.database.log.invest;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class InvestApi {

    public static DInvest createInvestment(DClient client, DStaffConductor conductor, Emeralds emeralds) {
        return createInvestEvent(client, conductor, emeralds, AccountEventType.INVEST);
    }

    public static DInvest createWithdrawal(DClient client, DStaffConductor conductor, Emeralds emeralds) {
        return createInvestEvent(client, conductor, emeralds.negative(), AccountEventType.WITHDRAWAL);
    }

    @NotNull
    private static DInvest createInvestEvent(DClient client, DStaffConductor conductor, Emeralds emeralds,
        AccountEventType type) {

        Instant date = Instant.now();
        DInvest investment = new DInvest(client, date, conductor, emeralds.amount(), type);
        try (Transaction transaction = DB.beginTransaction()) {
            investment.save(transaction);
            client.updateBalance(emeralds.amount(), date, transaction);
            transaction.commit();
        }
        return investment;
    }
}
