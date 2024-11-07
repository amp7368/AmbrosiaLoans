package com.ambrosia.loans.database.message;

import java.time.Instant;
import java.util.function.Function;

public enum RecentActivityType {
    LOAN_PAYMENT,
    OPEN_LOAN,
    LOAN_REQUEST;

    public RecentActivity toActivity(Instant date, Function<RecentActivity, String> msg) {
        return new RecentActivity(this, date, msg);
    }
}
