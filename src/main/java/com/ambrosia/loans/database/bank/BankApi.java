package com.ambrosia.loans.database.bank;

import com.ambrosia.loans.database.bank.query.QDBankSnapshot;
import com.ambrosia.loans.database.log.base.AccountEventType;
import java.sql.Timestamp;
import java.time.Instant;

public class BankApi {

    public static void updateBankBalance(long delta, Instant instant, AccountEventType eventType) {
        Timestamp timestamp = Timestamp.from(instant);
        DBankSnapshot snapshot = new QDBankSnapshot()
            .orderBy().date.desc()
            .setMaxRows(1)
            .findOne();
        long oldBalance = snapshot == null ? 0 : snapshot.getBalance();
        new DBankSnapshot(eventType, timestamp, oldBalance + delta, delta).save();
    }

}
