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

public class AlterLoanFrozen extends AlterLoan<Double> {

    private Instant effectiveDate;
    private double unfreezeRate;
    private Instant unfreezeDate;

    public AlterLoanFrozen() {
    }

    public AlterLoanFrozen(DLoan loan, Instant effectiveDate, double unfreezeRate, Instant unfreezeDate) {
        super(AlterChangeType.LOAN_FREEZE, loan, loan.getRateAt(effectiveDate), 0d);
        this.effectiveDate = effectiveDate;
        this.unfreezeRate = unfreezeRate;
        this.unfreezeDate = unfreezeDate;
    }


    @Override
    protected void apply(DLoan loan, Double current, Transaction transaction) {
        if (isApplied()) {
            loan.removeFreeze(effectiveDate, current, transaction);
        } else {
            loan.freeze(effectiveDate, unfreezeDate, unfreezeRate, current, transaction);
        }
    }

    @Override
    protected boolean isDependentInternal(AlterDB<?> dependency) {
        if (dependency instanceof AlterLoanFrozen depend) {
            // true if am I after or on dependency date?
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }
        return false;
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_RATE, AlterImpactedField.LOAN_END_DATE);
    }
}
