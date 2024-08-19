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
        return loan.getTotalOwed(null, date).negative();
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void createAdjustment(Emeralds difference, Instant date) {
        AdjustApi.createMigrationAdjustment(loan, difference, client(), date, true);
    }
}
