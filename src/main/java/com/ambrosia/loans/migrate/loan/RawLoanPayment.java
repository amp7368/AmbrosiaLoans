package com.ambrosia.loans.migrate.loan;

import java.util.Date;

public class RawLoanPayment {

    private long loanId;
    private Date date;
    private String paymentAmount;
    private String comments;

    @Override
    public String toString() {
        return "RawLoanPayment{" +
            "loanId=" + loanId +
            ", date='" + date + '\'' +
            ", paymentAmount='" + paymentAmount + '\'' +
            ", comments='" + comments + '\'' +
            '}';
    }
}
