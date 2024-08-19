package com.ambrosia.loans.discord.request.collateral;

import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestSender;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestReturnCollateral extends ActiveRequest<ActiveRequestReturnCollateralGui> {

    public ActiveRequestReturnCollateral(ActiveRequestType typeId,
        ActiveRequestSender sender) {
        super(typeId, sender);
    }

    @Override
    public @Nullable DAlterCreate onComplete() throws Exception {
        return null;
    }

    @Override
    public ActiveRequestReturnCollateralGui load() {
        return null;
    }
}
