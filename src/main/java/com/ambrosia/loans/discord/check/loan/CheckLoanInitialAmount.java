package com.ambrosia.loans.discord.check.loan;

import com.ambrosia.loans.discord.check.base.CheckPosAmount;
import com.ambrosia.loans.util.emerald.Emeralds;

public class CheckLoanInitialAmount extends CheckPosAmount {

    private static final Emeralds MIN_LOAN = Emeralds.leToEmeralds(16);
    private static final Emeralds MAX_LOAN = Emeralds.stxToEmeralds(50);

    public CheckLoanInitialAmount() {
        super(MIN_LOAN, MAX_LOAN);
    }
}
