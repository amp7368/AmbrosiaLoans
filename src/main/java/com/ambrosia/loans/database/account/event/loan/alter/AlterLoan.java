package com.ambrosia.loans.database.account.event.loan.alter;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;

public abstract class AlterLoan<T> extends AlterDBChange<DLoan, T> {

    public AlterLoan() {
    }

    public AlterLoan(AlterRecordType typeId, DLoan loan, T previous, T current) {
        super(typeId, loan.getId(), previous, current);
    }

    @Override
    public DLoan getEntity() {
        return LoanQueryApi.findById(this.getEntityId());
    }

    @Override
    public String getEntityTypeName() {
        return "Loan";
    }

}