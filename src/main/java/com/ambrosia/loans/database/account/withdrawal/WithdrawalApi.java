package com.ambrosia.loans.database.account.withdrawal;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.base.AccountEventApi;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.system.exception.NotEnoughFundsException;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import java.time.Instant;

public interface WithdrawalApi {

    static DWithdrawal createMigrationWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds) {
        return (DWithdrawal) AccountEventApi.createMigrationInvestLike(client, date, conductor, emeralds.negative(),
            AccountEventType.WITHDRAWAL);
    }

    static DWithdrawal createWithdrawal(DClient client, Instant date, DStaffConductor conductor, Emeralds emeralds)
        throws NotEnoughFundsException {
        Emeralds balance = client.getInvestBalanceNow();
        if (balance.amount() < emeralds.amount()) {
            NotEnoughFundsException e = new NotEnoughFundsException(emeralds, balance);
            DatabaseModule.get().logger().warn("", e);
            throw e;
        }
        return (DWithdrawal) AccountEventApi.createInvestLike(client, date, conductor, emeralds.negative(),
            AccountEventType.WITHDRAWAL);
    }

    interface WithdrawalQueryApi {

        static DWithdrawal findById(Long id) {
            return DB.find(DWithdrawal.class, id);
        }
    }
}
