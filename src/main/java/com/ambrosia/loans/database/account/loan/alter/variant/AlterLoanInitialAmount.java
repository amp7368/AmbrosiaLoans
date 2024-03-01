package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.alter.AlterLoan;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Transaction;
import java.util.Collection;
import java.util.List;

public class AlterLoanInitialAmount extends AlterLoan<Long> {

    public AlterLoanInitialAmount() {
    }

    public AlterLoanInitialAmount(DLoan loan, Emeralds amount) {
        super(AlterChangeType.LOAN_INITIAL_AMOUNT, loan, loan.getInitialAmount().amount(), amount.amount());
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.LOAN_INITIAL_AMOUNT);
    }

    @Override
    protected void apply(DLoan loan, Long value, Transaction transaction) {
        loan.setInitialAmount(Emeralds.of(value));
        loan.save(transaction);
        RunBankSimulation.simulateAsync(loan.getStartDate());
    }
}
