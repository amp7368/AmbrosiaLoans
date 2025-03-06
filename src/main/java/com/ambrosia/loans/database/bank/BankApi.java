package com.ambrosia.loans.database.bank;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import java.sql.Timestamp;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public class BankApi {

    public static void updateBankBalance(long delta, Instant instant, AccountEventType eventType) {
        Timestamp timestamp = Timestamp.from(instant);
        DBankSnapshot snapshot = getLatestSnapshot();
        long oldBalance = snapshot == null ? 0 : snapshot.getBalanceAmount();
        new DBankSnapshot(eventType, timestamp, oldBalance + delta, delta).save();
    }

    @Nullable
    public static DBankSnapshot getLatestSnapshot() {
        return new QDBankSnapshot()
            .orderBy().date.desc()
            .setMaxRows(1)
            .findOne();
    }
}
