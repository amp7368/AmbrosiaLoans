package com.ambrosia.loans.database.account.loan.interest.legacy;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.base.DLoanInterest;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class DLegacyInterest extends DLoanInterest<DLegacyInterestCalculator> {

    @Override
    public DLegacyInterestCalculator createCalculator(InterestCheckpoint checkpoint, Instant end) {
        return new DLegacyInterestCalculator(this, (DLegacyInterestCheckpoint) checkpoint, end);
    }

    @NotNull
    @Override
    public InterestCheckpoint createInitialCheckpoint(DLoan loan) {
        return new DLegacyInterestCheckpoint(loan);
    }
}
