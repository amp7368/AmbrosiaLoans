package com.ambrosia.loans.database.bank.monthly;

import com.ambrosia.loans.util.AmbrosiaTimeZone;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.math.BigDecimal;
import java.time.Instant;

public class BankMonthlySnapshot {

    protected Instant monthDate;
    protected BigDecimal delta;

    public BankMonthlySnapshot(Instant monthDate, BigDecimal delta) {
        this.monthDate = monthDate;
        this.delta = delta;
    }

    public Emeralds getDelta() {
        return Emeralds.of(delta);
    }

    public Instant getMonth() {
        return monthDate;
    }

    public int getYear() {
        return monthDate.atZone(AmbrosiaTimeZone.getTimeZoneId())
            .getYear();
    }
}
