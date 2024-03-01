package com.ambrosia.loans.database.account.withdrawal;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.base.AccountEventApi;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import java.time.Instant;

public interface WithdrawalApi {

    static DWithdrawal createWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds, boolean force) {
        Emeralds balance = client.getInvestBalance(date);
        if (balance.amount() < emeralds.amount()) {
            String msg = "Not enough emeralds! Tried withdrawing %s from %s investment".formatted(emeralds, balance);
            DatabaseModule.get().logger().error(msg);
            if (!force)
                throw new IllegalStateException(msg);
        }
        return (DWithdrawal) AccountEventApi.createInvestEvent(client, date, conductor, emeralds.negative(),
            AccountEventType.WITHDRAWAL);
    }

    interface WithdrawalQueryApi {

        static DWithdrawal findById(Long id) {
            return DB.find(DWithdrawal.class, id);
        }
    }
}
