package com.ambrosia.loans.database.account.event.investment.alter;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.alter.base.AlterImpactedField;
import com.ambrosia.loans.database.alter.gson.AlterChangeType;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AlterInvestmentDate extends AlterInvestment<Instant> {

    public AlterInvestmentDate() {
    }

    public AlterInvestmentDate(DInvestment investment, Instant current) {
        super(AlterChangeType.INVESTMENT_INSTANT, investment, investment.getDate(), current);
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.INVESTMENT_DATE);
    }

    @Override
    protected void apply(DInvestment investment, Instant value, Transaction transaction) {
        investment.setDate(value);
        investment.save(transaction);
    }
}
