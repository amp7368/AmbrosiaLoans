package com.ambrosia.loans.migrate.loan;

import com.ambrosia.loans.database.account.adjust.AdjustApi;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.migrate.RawMakeAdjustment;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import java.time.Instant;

public record ImportedLoanAdjustment(DLoan loan, Instant date, Emeralds amount, DClient client) implements RawMakeAdjustment {

    @Override
    public Emeralds getBalanceAt(Instant date) {
        loan.refresh();
        loan.getSections().forEach(Model::refresh);
        // todo
//        Duration loanDuration = Duration.between(loan.getStartDate(), date);
//        double weeks = loanDuration.toSeconds() / (double) Duration.ofDays(7).toSeconds();
//        double margin = 0.5 / 7.0;
//        double up = Math.ceil(weeks + margin);
//        double down = Math.ceil(weeks - margin);
//        if (down == 0)
//            date = date.plus(up == 0 ? 12 : 0, ChronoUnit.HOURS);
//        else if (up != down)
//            date = date.plus(12, ChronoUnit.HOURS);
//        client.refresh();
//        return client.getLoanBalance(date);
        Emeralds negative = loan.getTotalOwed(null, date).negative();
        if (loan.getId() == 129) {
            System.err.println("Client 129 total owed = " + negative);
        }
        return negative;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void createAdjustment(Emeralds difference, Instant date) {
        AdjustApi.createAdjustment(loan, difference, client(), date, false);
    }
}
