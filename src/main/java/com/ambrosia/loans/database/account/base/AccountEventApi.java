package com.ambrosia.loans.database.account.base;

import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;

public interface AccountEventApi {

    static AccountEvent createMigrationInvestLike(DClient client, Instant date, DStaffConductor staff, Emeralds emeralds,
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

    static AccountEvent createInvestLike(DClient client, Instant date, DStaffConductor staff, Emeralds amount,
        AccountEventType eventType) {
        AccountEvent event;
        if (eventType == AccountEventType.INVEST) {
            DInvestment investment = new DInvestment(client, date, staff, amount, eventType);
            event = investment;
            event.getClient().addInvestment(investment);
        } else {
            DWithdrawal withdrawal = new DWithdrawal(client, date, staff, amount, eventType);
            event = withdrawal;
            event.getClient().addWithdrawal(withdrawal);
        }
        event.save();
        AlterCreateType alterCreateType = eventType.getAlterCreateType();
        AlterCreateApi.create(staff, alterCreateType, event.getId());
        event.refresh();
        event.getClient().refresh();

        RunBankSimulation.simulateAsync(event.getDate());
        return event;
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
