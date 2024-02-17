package com.ambrosia.loans.database.account.event.loan.alter.variant;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Transaction;

public class AlterLoanInitialAmount extends AlterLoan<Long> {

    public AlterLoanInitialAmount() {
    }

    public AlterLoanInitialAmount(DLoan loan, Emeralds amount) {
        super(AlterRecordType.LOAN_INITIAL_AMOUNT, loan, loan.getInitialAmount().amount(), amount.amount());
    }


    @Override
    protected void apply(DLoan loan, Long value, Transaction transaction) {
        loan.setInitialAmount(Emeralds.of(value));
        loan.save(transaction);
    }
}
