package com.ambrosia.loans.database.account.event.adjust;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public interface AdjustApi {

    static void createAdjustment(Emeralds difference, DClient client, Instant date, boolean updateBalance) {
        createAdjustment(null, difference, client, date, updateBalance);
    }

    static void createAdjustment(@Nullable DLoan loan, Emeralds difference, DClient client, Instant date,
        boolean updateBalance) {
        try (Transaction transaction = DB.beginTransaction()) {
            createAdjustment(loan, difference, client, date, updateBalance, transaction);
            transaction.commit();
        }
    }

    static void createAdjustment(@Nullable DLoan loan, Emeralds difference, DClient client, Instant date,
        boolean updateBalance, Transaction transaction) {
        if (difference.isZero()) return;

        AccountEventType type;
        if (loan != null) type = AccountEventType.ADJUST_LOAN;
        else if (difference.isPositive()) type = AccountEventType.ADJUST_UP;
        else type = AccountEventType.ADJUST_DOWN;

        if (loan == null) {
            DAdjustBalance adjustment = new DAdjustBalance(client, date, DStaffConductor.MIGRATION, difference, type);
            adjustment.save(transaction);
        } else {
            DAdjustLoan adjustment = new DAdjustLoan(loan, date, DStaffConductor.MIGRATION, difference, type);
            adjustment.save(transaction);
            loan.refresh();
        }
        if (updateBalance)
            client.updateBalance(difference.amount(), date, type, transaction);
    }
}
