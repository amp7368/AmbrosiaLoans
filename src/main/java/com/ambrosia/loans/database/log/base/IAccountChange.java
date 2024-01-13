package com.ambrosia.loans.database.log.base;

import java.time.Instant;

public interface IAccountChange {

    Instant getDate();

    void updateSimulation();

    AccountEventType getEventType();
}
