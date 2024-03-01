package com.ambrosia.loans.database.account.base;

import com.ambrosia.loans.database.entity.client.DClient;
import java.time.Instant;
import java.util.Comparator;

public interface IAccountChange {

    Comparator<? super IAccountChange> ORDER = Comparator.comparing(IAccountChange::getDate)
        .thenComparing(IAccountChange::getEventType, AccountEventType.ORDER);

    DClient getClient();

    Instant getDate();

    void updateSimulation();

    AccountEventType getEventType();
}
