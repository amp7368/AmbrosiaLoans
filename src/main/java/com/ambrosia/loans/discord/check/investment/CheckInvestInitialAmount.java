package com.ambrosia.loans.discord.check.investment;

import com.ambrosia.loans.discord.check.base.CheckPosAmount;
import com.ambrosia.loans.util.emerald.Emeralds;

public class CheckInvestInitialAmount extends CheckPosAmount {

    private static final Emeralds MIN_INVEST = Emeralds.leToEmeralds(16);
    private static final Emeralds MAX_INVEST = Emeralds.stxToEmeralds(50);

    public CheckInvestInitialAmount() {
        super(MIN_INVEST, MAX_INVEST);
    }
}
