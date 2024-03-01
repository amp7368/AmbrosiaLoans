package com.ambrosia.loans.database.account.event.investment.alter;

import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.investment.InvestApi.InvestQueryApi;
import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.gson.AlterChangeType;

public abstract class AlterInvestment<T> extends AlterDBChange<DInvestment, T> {

    public AlterInvestment() {
    }

    public AlterInvestment(AlterChangeType typeId, DInvestment entity, T previous, T current) {
        super(typeId, entity.getId(), previous, current);
    }

    @Override
    public String getEntityType() {
        return "Investment";
    }

    @Override
    public DInvestment getEntity() {
        return InvestQueryApi.findById(getEntityId());
    }
}
