package com.ambrosia.loans.database.account.event.loan.alter.variant;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.base.AlterImpactedField;
import com.ambrosia.loans.database.alter.gson.AlterChangeType;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AlterLoanStartDate extends AlterLoan<Instant> {

    public AlterLoanStartDate() {
    }

    public AlterLoanStartDate(DLoan loan, Instant current) {
        super(AlterChangeType.LOAN_START_DATE, loan, loan.getStartDate(), current);
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_START_DATE);
    }

    @Override
    protected void apply(DLoan loan, Instant value, Transaction transaction) {
        loan.setStartDate(value);
        loan.save(transaction);
    }
}
