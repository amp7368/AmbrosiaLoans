package com.ambrosia.loans.database.loan;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DLoanSnapshot {

    @Column
    public double rate;
    @Column
    public DLoanStatus status;
    @Column
    public int totalOwed;
    @Column
    public int partialPay;

    public DLoanSnapshot(DLoan loan) {
        this.rate = loan.rate;
        this.status = loan.status;
        this.totalOwed = loan.moment.totalOwed;
        this.partialPay = loan.moment.partialPay;
    }

    public DLoanSnapshot() {
    }
}
