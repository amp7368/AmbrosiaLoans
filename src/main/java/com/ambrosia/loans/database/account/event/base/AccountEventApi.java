package com.ambrosia.loans.database.account.event.base;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.gson.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;

public interface AccountEventApi {

    static AccountEvent createInvestEvent(DClient client, Instant date, DStaffConductor staff, Emeralds emeralds,
        AccountEventType type) {
        AccountEvent investment;
        if (type == AccountEventType.INVEST)
            investment = new DInvestment(client, date, staff, emeralds, type);
        else
            investment = new DWithdrawal(client, date, staff, emeralds, type);

        try (Transaction transaction = DB.beginTransaction()) {
            client.updateBalance(emeralds.amount(), date, type, transaction);
            investment.save(transaction);
            transaction.commit();
        }
        return investment;
    }

    static AccountEvent createInvestLike(BaseActiveRequestInvest<?> request) throws InvalidStaffConductorException {
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
        AlterCreateType alterCreateType = request.getEventType().getAlterCreateType();
        AlterCreateApi.create(request.getConductor(), alterCreateType, event.getId());
        event.refresh();
        event.getClient().refresh();

        RunBankSimulation.simulateAsync(event.getDate());
        return event;
    }
}
