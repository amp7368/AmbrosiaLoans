package com.ambrosia.loans.database.account.loan.interest.standard;

import com.ambrosia.loans.database.account.DClientLoanSnapshot;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import java.time.Instant;

public class DStandardInterestCheckpoint extends InterestCheckpoint {

    private long principal; // positive

    public DStandardInterestCheckpoint() {
    }

    public DStandardInterestCheckpoint(DClientLoanSnapshot snapshot) {
        super(snapshot.getLoan());
        this.balance = Math.abs(snapshot.getBalance().amount());
        this.lastUpdated = snapshot.getDate();
        this.principal = Math.abs(snapshot.getPrincipal());
    }

    public DStandardInterestCheckpoint(DLoan loan) {
        super(loan);
        this.principal = loan.getInitialAmount().amount();
    }

    public DStandardInterestCheckpoint(DStandardInterestCheckpoint other) {
        super(other);
        this.principal = other.principal;
    }

    @Override
    public void updateBalance(long delta, Instant date) {
        super.updateBalance(delta, date);
        this.principal = Math.min(this.principal, this.balance);
        this.principal = Math.max(this.principal, 0);
    }

    @Override
    public DStandardInterestCheckpoint copy() {
        return new DStandardInterestCheckpoint(this);
    }

    public long getPrincipal() {
        return principal;
    }
}
