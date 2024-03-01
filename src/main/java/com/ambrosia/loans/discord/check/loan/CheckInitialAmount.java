package com.ambrosia.loans.discord.check.loan;

import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.util.emerald.Emeralds;

public class CheckInitialAmount extends CheckError<Emeralds> {

    private static final Emeralds MIN_LOAN = Emeralds.leToEmeralds(16);
    private static final Emeralds MAX_LOAN = Emeralds.stxToEmeralds(50);


    @Override
    public void checkAll(Emeralds amount, CheckErrorList error) {
        if (amount.isNegative()) {
            error.addError("'Initial amount' must be positive!");
        }
        if (amount.lt(MIN_LOAN.amount())) {
            String msg = "Set 'initial amount' to %s. Are you sure you want to set it under %s".formatted(amount, MIN_LOAN);
            error.addWarning(msg);
        }
        if (amount.gt(MAX_LOAN.amount())) {
            String msg = "Set 'initial amount' to %s. Are you sure you want to set it over %s?".formatted(amount, MAX_LOAN);
            error.addWarning(msg);
        }
    }
}
