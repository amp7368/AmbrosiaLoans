package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.alter.AlterCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class AlterCollateralStatus extends AlterCollateral<DCollateralStatus> {

    protected Instant effectiveDate;

    public AlterCollateralStatus() {
    }

    public AlterCollateralStatus(DCollateral collateral, Instant effectiveDate, DCollateralStatus status) {
        super(AlterChangeType.COLLATERAL_STATUS, collateral, collateral.getStatus(), status);
        this.effectiveDate = effectiveDate;
    }

    @Override
    protected void apply(DCollateral collateral, DCollateralStatus newValue, Transaction transaction) {
        switch (newValue) {
            case COLLECTED -> collateral.setCollected();
            case RETURNED -> collateral.setReturned(effectiveDate);
            case SOLD -> collateral.setSold(effectiveDate);
        }
        collateral.save(transaction);
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.COLLATERAL_STATUS);
    }
}
