package com.ambrosia.loans.discord.check.base;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CheckDate extends CheckError<Instant> {

    @Override
    public void checkAll(Instant value, CheckErrorList error) {
        if (value.isAfter(Instant.now())) {
            error.addError("%s is in the future!".formatted(formatDate(value)));
        }
        Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        if (value.isBefore(monthAgo)) {
            String msg = "Cannot set to %s since it is more than a month ago!"
                .formatted(formatDate(value));
            error.addError(msg);
        }
    }
}
