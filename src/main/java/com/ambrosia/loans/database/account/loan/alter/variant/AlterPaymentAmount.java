package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.alter.AlterPayment;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Transaction;
import java.util.Collection;
import java.util.List;

public class AlterPaymentAmount extends AlterPayment<Emeralds> {

    public AlterPaymentAmount() {
    }

    public AlterPaymentAmount(DLoanPayment payment, Emeralds current) {
        super(AlterChangeType.PAYMENT_AMOUNT, payment, payment.getAmount(), current);
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_PAYMENT_AMOUNT);
    }


    @Override
    protected void apply(DLoanPayment payment, Emeralds value, Transaction transaction) {
        payment.setAmount(value);
        payment.save();
        RunBankSimulation.simulateAsync(payment.getDate());
    }
}
