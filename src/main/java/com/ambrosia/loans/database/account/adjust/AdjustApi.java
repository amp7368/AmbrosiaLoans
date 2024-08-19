package com.ambrosia.loans.database.account.adjust;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public interface AdjustApi {

    static void createMigrationAdjustment(Emeralds difference, DClient client, Instant date, boolean updateBalance) {
        createMigrationAdjustment(null, difference, client, date, updateBalance);
    }

    static void createMigrationAdjustment(@Nullable DLoan loan, Emeralds difference, DClient client, Instant date,
        boolean updateBalance) {
        if (difference.isZero()) return;

        AccountEventType type;
        if (loan != null) type = AccountEventType.ADJUST_LOAN;
        else if (difference.isPositive()) type = AccountEventType.ADJUST_UP;
        else type = AccountEventType.ADJUST_DOWN;

        if (loan == null) {
            DAdjustBalance adjustment = new DAdjustBalance(client, date, DStaffConductor.MIGRATION, difference, type);
            adjustment.save();
        } else {
            DAdjustLoan adjustment = new DAdjustLoan(loan, date, DStaffConductor.MIGRATION, difference, type);
            adjustment.save();
            loan.refresh();
        }
        if (updateBalance)
            client.updateBalance(difference.amount(), date, type);
    }

    static DAdjustLoan createAdjustment(DStaffConductor staff, DLoan loan, Emeralds amount, Instant date) {
        DAdjustLoan adjustment;
        try (Transaction transaction = DB.beginTransaction()) {
            adjustment = new DAdjustLoan(loan, date, staff, amount, AccountEventType.ADJUST_LOAN);
            adjustment.save(transaction);
            transaction.commit();
        }
        AlterCreateApi.create(staff, AlterCreateType.ADJUST_LOAN, adjustment.getId());
        return adjustment;
    }
}
