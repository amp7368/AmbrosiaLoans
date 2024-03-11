package com.ambrosia.loans.discord.check.base;

import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.util.emerald.Emeralds;

public class CheckPosAmount extends CheckError<Emeralds> {

    private final Emeralds warnLowerBound;
    private final Emeralds warnUpperBound;

    public CheckPosAmount(Emeralds warnLowerBound, Emeralds warnUpperBound) {
        if (warnLowerBound.gte(warnUpperBound.amount())) {
            throw new IllegalStateException("CheckLower of %s >= CheckUpper %s!".formatted(warnLowerBound, warnUpperBound));
        }
        this.warnLowerBound = warnLowerBound;
        this.warnUpperBound = warnUpperBound;
    }

    @Override
    public void checkAll(Emeralds amount, CheckErrorList error) {
        if (!amount.isPositive()) {
            error.addError("'Initial amount' must be positive! Provided: %s.".formatted(amount));
        }
        if (amount.lt(warnLowerBound.amount())) {
            String msg = "Setting 'initial amount' to %s. Are you sure you want to set it under %s".formatted(amount, warnLowerBound);
            error.addWarning(msg);
        }
        if (amount.gt(warnUpperBound.amount())) {
            String msg = "Setting 'initial amount' to %s. Are you sure you want to set it over %s?".formatted(amount, warnUpperBound);
            error.addWarning(msg);
        }
    }
}
