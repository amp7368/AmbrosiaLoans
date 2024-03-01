package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
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
