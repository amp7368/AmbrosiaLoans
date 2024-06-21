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

public class AlterLoanUnfreeze extends AlterLoan<Double> {

    Instant effectiveDate;
    private double beforeFreezeRate;
    private Instant unfreezeDate;

    public AlterLoanUnfreeze() {
    }

    public AlterLoanUnfreeze(DLoan loan, Instant effectiveDate, double beforeFreezeRate, double unfreezeToRate,
        Instant previousUnfreezeDate) {
        super(AlterChangeType.LOAN_FREEZE, loan, 0d, unfreezeToRate);
        this.effectiveDate = effectiveDate;
        this.beforeFreezeRate = beforeFreezeRate;
        this.unfreezeDate = previousUnfreezeDate;
    }

    @Override
    protected void apply(DLoan loan, Double current, Transaction transaction) {
        if (isApplied()) {
            loan.freeze(effectiveDate, unfreezeDate, beforeFreezeRate, current, transaction);
        } else {
            loan.unfreezeLoan(current, effectiveDate, transaction);
        }
    }

    @Override
    protected boolean isDependentInternal(AlterDB<?> dependency) {
        if (dependency instanceof AlterLoanUnfreeze depend) {
            // true if am I after or on dependency date?
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }
        if (dependency instanceof AlterLoanFreeze depend) {
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }
        return false;
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_RATE, AlterImpactedField.LOAN_END_DATE);
    }
}

