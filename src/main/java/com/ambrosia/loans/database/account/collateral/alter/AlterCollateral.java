package com.ambrosia.loans.database.account.collateral.alter;

import com.ambrosia.loans.database.account.collateral.CollateralApi;
import com.ambrosia.loans.database.account.collateral.DCollateral;
import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.alter.type.AlterCreateType;

public abstract class AlterCollateral<T> extends AlterDBChange<DCollateral, T> {

    public AlterCollateral() {
    }

    public AlterCollateral(AlterChangeType typeId, DCollateral collateral, T previous, T current) {
        super(typeId, collateral.getId(), previous, current);
    }

    @Override
    public DCollateral getEntity() {
        long id = this.getEntityId();
        return CollateralApi.findById(id);
    }

    @Override
    public AlterCreateType getEntityType() {
        return AlterCreateType.COLLATERAL;
    }

}
