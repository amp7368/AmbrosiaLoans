package com.ambrosia.loans.database.account.event.investment.alter;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.alter.base.AlterImpactedField;
import com.ambrosia.loans.database.alter.gson.AlterChangeType;
import com.ambrosia.loans.database.system.service.RunBankSimulation;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Transaction;
import java.util.Collection;
import java.util.List;

public class AlterInvestmentAmount extends AlterInvestment<Emeralds> {

    public AlterInvestmentAmount() {
    }

    public AlterInvestmentAmount(DInvestment investment, Emeralds amount) {
        super(AlterChangeType.INVESTMENT_AMOUNT, investment, investment.getDeltaAmount(), amount);
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.INVESTMENT_AMOUNT);
    }

    @Override
    protected void apply(DInvestment investment, Emeralds value, Transaction transaction) {
        investment.setDeltaAmount(value);
        investment.save(transaction);
        RunBankSimulation.simulateAsync(investment.getDate());
    }
}
