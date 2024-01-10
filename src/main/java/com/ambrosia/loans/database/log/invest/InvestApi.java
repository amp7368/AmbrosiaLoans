package com.ambrosia.loans.database.log.invest;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import java.time.Instant;

public class InvestApi {

    public static DInvest createInvestment(DClient client, DStaffConductor conductor, Emeralds emeralds) {
        DInvest investment = new DInvest(client.getAccountLog(), Instant.now(), conductor, emeralds.amount());
        investment.save();
        return investment;
    }
}
