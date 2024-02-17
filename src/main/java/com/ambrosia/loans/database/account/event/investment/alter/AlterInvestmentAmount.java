package com.ambrosia.loans.database.account.event.investment.alter;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Transaction;

public class AlterInvestmentAmount extends AlterInvestment<Emeralds> {

    public AlterInvestmentAmount() {
    }

    public AlterInvestmentAmount(DInvestment investment, Emeralds amount) {
        super(AlterRecordType.INVESTMENT_AMOUNT, investment, investment.getDeltaAmount(), amount);
    }

    @Override
    protected void apply(DInvestment investment, Emeralds value, Transaction transaction) {
        investment.setDeltaAmount(value);
        investment.save(transaction);
    }
}
