package com.ambrosia.loans.database.account.loan.alter;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.database.alter.type.AlterChangeType;

public abstract class AlterLoan<T> extends AlterDBChange<DLoan, T> {

    public AlterLoan() {
    }

    public AlterLoan(AlterChangeType typeId, DLoan loan, T previous, T current) {
        super(typeId, loan.getId(), previous, current);
    }

    @Override
    public DLoan getEntity() {
        return LoanQueryApi.findById(this.getEntityId());
    }

    @Override
    public String getEntityType() {
        return "Loan";
    }

}
