package com.ambrosia.loans.database.account.event.base;

import java.time.Instant;

public interface IAccountChange {

    Instant getDate();

    void updateSimulation();

    AccountEventType getEventType();
}
