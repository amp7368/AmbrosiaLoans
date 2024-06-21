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

public class AlterLoanFreeze extends AlterLoan<Double> {

    Instant effectiveDate;
    private double unfreezeToRate;
    private Instant unfreezeDate;
    private Instant pastUnfreezeDate;
    private Double pastUnfreezeRate;

    public AlterLoanFreeze() {
    }

    public AlterLoanFreeze(DLoan loan, Instant effectiveDate, double unfreezeToRate, Instant unfreezeDate, Instant pastUnfreezeDate,
        Double pastUnfreezeRate) {
        super(AlterChangeType.LOAN_FREEZE, loan, loan.getRateAt(effectiveDate), 0d);
        this.effectiveDate = effectiveDate;
        this.unfreezeToRate = unfreezeToRate;
        this.unfreezeDate = unfreezeDate;
        this.pastUnfreezeDate = pastUnfreezeDate;
        this.pastUnfreezeRate = pastUnfreezeRate;
    }


    @Override
    protected void apply(DLoan loan, Double current, Transaction transaction) {
        if (isApplied()) {
            loan.deletePastFreeze(effectiveDate, current, pastUnfreezeDate, pastUnfreezeRate, transaction);
        } else {
            loan.freeze(effectiveDate, unfreezeDate, unfreezeToRate, current, transaction);
        }
    }

    @Override
    protected boolean isDependentInternal(AlterDB<?> dependency) {
        if (dependency instanceof AlterLoanFreeze depend) {
            // true if am I after or on dependency date?
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }
        if (dependency instanceof AlterLoanUnfreeze depend) {
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }

        return false;
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_RATE, AlterImpactedField.LOAN_END_DATE);
    }
}
