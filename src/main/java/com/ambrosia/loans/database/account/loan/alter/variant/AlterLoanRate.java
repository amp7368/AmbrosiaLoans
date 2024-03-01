package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.change.AlterDB;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AlterLoanRate extends AlterLoan<Double> {

    private Instant effectiveDate;

    public AlterLoanRate() {
    }

    public AlterLoanRate(DLoan loan, Instant effectiveDate, double current) {
        super(AlterChangeType.LOAN_RATE, loan, loan.getRateAt(effectiveDate), current);
        this.effectiveDate = effectiveDate;
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_RATE);
    }

    @Override
    protected void apply(DLoan loan, Double value, Transaction transaction) {
        loan.changeToNewRate(value, effectiveDate, transaction);
    }

    @Override
    protected boolean isDependentInternal(AlterDB<?> dependency) {
        if (dependency instanceof AlterLoanRate depend) {
            // true if am I after or on dependency date?
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }
        return false;
    }
}
