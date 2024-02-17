package com.ambrosia.loans.database.account.event.investment.alter;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import io.ebean.Transaction;
import java.time.Instant;

public class AlterInvestmentDate extends AlterInvestment<Instant> {

    public AlterInvestmentDate() {
    }

    public AlterInvestmentDate(DInvestment investment, Instant current) {
        super(AlterRecordType.INVESTMENT_INSTANT, investment, investment.getDate(), current);
    }

    @Override
    protected void apply(DInvestment investment, Instant value, Transaction transaction) {
        investment.setDate(value);
        investment.save(transaction);
    }
}
