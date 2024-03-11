package com.ambrosia.loans.discord.check.withdrawal;

import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckPosAmount;
import com.ambrosia.loans.util.emerald.Emeralds;

public class CheckWithdrawalAmount extends CheckPosAmount {

    private static final Emeralds MIN_WITHDRAWAL = Emeralds.of(1);
    private static final Emeralds MAX_WITHDRAWAL = Emeralds.stxToEmeralds(50);
    private final Emeralds availableFunds;

    public CheckWithdrawalAmount(Emeralds availableFunds) {
        super(MIN_WITHDRAWAL, MAX_WITHDRAWAL);
        this.availableFunds = availableFunds;
    }

    @Override
    public void checkAll(Emeralds amount, CheckErrorList error) {
        if (amount.gt(availableFunds.amount())) {
            String msg = "Not enough emeralds! Tried withdrawing %s from %s investment."
                .formatted(amount, availableFunds);
            error.addError(msg);
        }
        super.checkAll(amount, error);
    }
}
