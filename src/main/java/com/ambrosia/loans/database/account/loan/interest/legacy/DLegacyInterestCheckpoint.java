package com.ambrosia.loans.database.account.loan.interest.legacy;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.standard.DStandardInterestCheckpoint;

public class DLegacyInterestCheckpoint extends DStandardInterestCheckpoint {

    public DLegacyInterestCheckpoint() {
    }

    public DLegacyInterestCheckpoint(DLoan loan) {
        super(loan);
    }

    public DLegacyInterestCheckpoint(DLegacyInterestCheckpoint other) {
        super(other);
    }

    @Override
    public DLegacyInterestCheckpoint copy() {
        return new DLegacyInterestCheckpoint(this);
    }
}
