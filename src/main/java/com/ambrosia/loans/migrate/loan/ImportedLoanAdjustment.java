package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.database.account.event.investment.InvestApi;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.migrate.base.RawMakeAdjustment;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ImportedLoanAdjustment(DLoan loan, Instant date, Emeralds amount, DClient client) implements RawMakeAdjustment {

    @Override
    public Emeralds getBalanceAt(Instant date) {
        loan.refresh();
        Duration loanDuration = Duration.between(loan.getStartDate(), date);
        double weeks = loanDuration.toSeconds() / (double) Duration.ofDays(7).toSeconds();
        double margin = 0.5 / 7.0;
        double up = Math.ceil(weeks + margin);
        double down = Math.ceil(weeks - margin);
        if (down == 0)
            date = date.plus(up == 0 ? 6 : 0, ChronoUnit.HOURS);
        else if (up != down)
            date = date.plus(6, ChronoUnit.HOURS);
        return loan.getTotalOwed(null, date).negative();
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void createAdjustment(Emeralds difference, Instant date) {
        InvestApi.createAdjustment(loan, difference, client(), date, false);
    }
}
