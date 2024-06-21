package com.ambrosia.loans.discord.check.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.discord.check.CheckErrorList;

public class CheckRate {


    public static void checkAll(CheckErrorList error, double rate) {
        if (rate < 0) {
            error.addError("Rate must be positive!");
        } else if (rate < 1) {
            String msg = "Set rate as %s. Are you sure you want to set it less than 1%%?".formatted(formatPercentage(rate / 100));
            error.addWarning(msg);
        } else if (rate > 10) {
            String msg = "Set rate as %s. Are you sure you want to set it more than 10%%?".formatted(formatPercentage(rate / 100));
            error.addWarning(msg);
        }
    }
}
