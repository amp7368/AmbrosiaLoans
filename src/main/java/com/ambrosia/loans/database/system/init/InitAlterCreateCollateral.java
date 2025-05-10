package com.ambrosia.loans.database.system.init;

import com.ambrosia.loans.database.account.collateral.query.QDCollateral;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterCreateApi;
import com.ambrosia.loans.database.alter.create.query.QDAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import java.util.List;

public class InitAlterCreateCollateral {

    public static void verify() {
        List<Long> collateralIds = new QDCollateral().findIds();
        collateralIds.forEach(InitAlterCreateCollateral::verify);
    }

    private static void verify(long collateralId) {
        boolean exists = new QDAlterCreate().where()
            .entityType.eq(AlterCreateType.COLLATERAL.getTypeId())
            .entityId.eq(collateralId)
            .exists();
        if (exists) return;

        AlterCreateApi.create(DStaffConductor.SYSTEM, AlterCreateType.COLLATERAL, collateralId);
    }

}
