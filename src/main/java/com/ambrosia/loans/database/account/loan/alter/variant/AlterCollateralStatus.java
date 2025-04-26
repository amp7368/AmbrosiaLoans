package com.ambrosia.loans.database.account.loan.alter.variant;

import com.ambrosia.loans.database.account.loan.alter.AlterCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.database.alter.change.AlterImpactedField;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class AlterCollateralStatus extends AlterCollateral<DCollateralStatus> {

    protected Instant effectiveDate;
    @Nullable
    protected Emeralds soldForAmount = null;
    @Nullable
    protected Emeralds previousSoldForAmount = null;

    public AlterCollateralStatus() {
    }

    public AlterCollateralStatus(
        DCollateral collateral,
        Instant effectiveDate,
        DCollateralStatus status,
        @Nullable Emeralds soldForAmount
    ) {
        super(AlterChangeType.COLLATERAL_STATUS, collateral, collateral.getStatus(), status);
        this.effectiveDate = effectiveDate;
        this.soldForAmount = soldForAmount;
        this.previousSoldForAmount = collateral.getSoldForAmount();
    }

    @Override
    protected void apply(DCollateral collateral, DCollateralStatus newValue, Transaction transaction) {
        switch (newValue) {
            case NOT_COLLECTED, COLLECTED -> collateral.setStatus(newValue, null, previousSoldForAmount);
            default -> collateral.setStatus(newValue, effectiveDate, soldForAmount);
        }
        collateral.save(transaction);
    }

    @Override
    protected Collection<AlterImpactedField> initImpactedFields() {
        return List.of(AlterImpactedField.COLLATERAL_STATUS, AlterImpactedField.COLLATERAL_SOLD_FOR);
    }
}
