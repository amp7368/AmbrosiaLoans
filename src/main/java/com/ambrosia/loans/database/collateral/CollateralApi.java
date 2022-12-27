package com.ambrosia.loans.database.collateral;

import com.ambrosia.loans.database.base.ModelApi;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.database.util.UniqueMessages;

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
