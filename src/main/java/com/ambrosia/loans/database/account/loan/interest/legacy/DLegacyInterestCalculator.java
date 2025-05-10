package com.ambrosia.loans.database.account.loan.interest.legacy;

import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import com.ambrosia.loans.database.account.loan.interest.base.LoanInterestCalculator;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class DLegacyInterestCalculator extends LoanInterestCalculator<DLegacyInterest, InterestCheckpoint> {

    public DLegacyInterestCalculator(DLegacyInterest settings, DLegacyInterestCheckpoint checkpoint, Instant end) {
        super(settings, checkpoint, end);
    }

    @NotNull
    @Override
    public InterestCheckpoint getInterest() {
        // todo implement
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
