package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AlterLoanDefaulted extends AlterLoan<Boolean> {

    protected Instant date;

    public AlterLoanDefaulted() {
    }

    public AlterLoanDefaulted(DLoan loan, boolean isDefaulted, Instant date) {
        super(AlterChangeType.LOAN_DEFAULTED, loan, loan.isDefaulted(), isDefaulted);
        this.date = date;
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_END_DATE);
    }

    @Override
    protected void apply(DLoan loan, Boolean value, Transaction transaction) {
        loan.setDefaulted(date, value)
            .save(transaction);
    }
}
