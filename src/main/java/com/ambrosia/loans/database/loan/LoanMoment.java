package com.ambrosia.loans.database.loan;

import javax.persistence.Entity;

@Entity
public class LoanMoment {

    public int totalOwed;
    public int partialPay;

    public LoanMoment(int totalOwed) {
        this.totalOwed = totalOwed;
        this.partialPay = 0;
    }
}
