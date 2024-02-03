package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.migrate.base.RawMakeAdjustment;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;

public record ImportedLoanAdjustment(DLoan loan, Instant date, Emeralds amount, DClient client) implements RawMakeAdjustment {

    @Override
    public Emeralds getBalanceAt(Instant date) {
        loan.refresh();
        return loan.getTotalOwed(date).negative();
    }

    @Override
    public long getId() {
        return 0;
    }
}
