package com.ambrosia.loans.database.account.loan.interest.standard;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.base.DLoanInterest;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class DStandardInterest extends DLoanInterest<StandardCalculator> {

    public DStandardInterest() {
    }

    @Override
    public StandardCalculator createCalculator(InterestCheckpoint checkpoint, Instant end) {
        return new StandardCalculator(this, (DStandardInterestCheckpoint) checkpoint, end);
    }

    @NotNull
    @Override
    public InterestCheckpoint createInitialCheckpoint(DLoan loan) {
        return new DStandardInterestCheckpoint(loan);
    }
}
