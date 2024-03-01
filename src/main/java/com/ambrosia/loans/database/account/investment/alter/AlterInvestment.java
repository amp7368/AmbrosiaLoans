package com.ambrosia.loans.database.account.investment.alter;

import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.investment.InvestApi.InvestQueryApi;
import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.database.alter.type.AlterChangeType;

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
