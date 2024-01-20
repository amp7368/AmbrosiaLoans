package com.ambrosia.loans.database.account.event.base;

import com.ambrosia.loans.database.entity.client.DClient;
import java.time.Instant;

public interface IAccountChange {

    DClient getClient();

    Instant getDate();

    void updateSimulation();

    AccountEventType getEventType();
}
