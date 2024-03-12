package com.ambrosia.loans.database.account.loan.alter;

import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.alter.type.AlterCreateType;

public abstract class AlterPayment<T> extends AlterDBChange<DLoanPayment, T> {

    public AlterPayment() {
    }

    public AlterPayment(AlterChangeType typeId, DLoanPayment payment, T previous, T current) {
        super(typeId, payment.getId(), previous, current);
    }

    @Override
    public DLoanPayment getEntity() {
        return LoanQueryApi.findPaymentById(getEntityId());
    }

    @Override
    public AlterCreateType getEntityType() {
        return AlterCreateType.PAYMENT;
    }
}
