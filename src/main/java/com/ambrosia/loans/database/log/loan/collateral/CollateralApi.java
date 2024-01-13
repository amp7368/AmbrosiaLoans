package com.ambrosia.loans.database.log.loan.collateral;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.base.util.UniqueMessages;

public class CollateralApi extends ModelApi<DCollateral> {

    public CollateralApi(DCollateral entity) {
        super(entity);
    }

    public static CollateralApi createCollateral(String link) throws CreateEntityException {
        DCollateral collateral = new DCollateral(link);
        UniqueMessages.saveIfUnique(collateral);
        return api(collateral);
    }

    private static CollateralApi api(DCollateral loan) {
        return new CollateralApi(loan);
    }
}
