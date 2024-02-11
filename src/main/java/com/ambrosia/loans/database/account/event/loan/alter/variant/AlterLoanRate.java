package com.ambrosia.loans.database.account.event.loan.alter.variant;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import io.ebean.Transaction;
import java.time.Instant;

public class AlterLoanRate extends AlterLoan<Double> {

    private Instant effectiveDate;

    public AlterLoanRate() {
    }

    public AlterLoanRate(DLoan loan, Instant effectiveDate, double current) {
        super(AlterRecordType.LOAN_RATE, loan, loan.getRateAt(effectiveDate), current);
        this.effectiveDate = effectiveDate;
    }

    @Override
    protected void apply(DLoan loan, Double value, Transaction transaction) {
        loan.changeToNewRate(value, effectiveDate, transaction);
    }

    @Override
    public boolean isDependentInternal(AlterDBChange<?, ?> dependency) {
        if (dependency instanceof AlterLoanRate depend) {
            // am I after or on dependency date?
            return !depend.effectiveDate.isBefore(this.effectiveDate);
        }
        return false;
    }
}
