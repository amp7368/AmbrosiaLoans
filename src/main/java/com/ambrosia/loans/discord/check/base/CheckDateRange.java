package com.ambrosia.loans.discord.check.base;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.discord.check.CheckError;
import com.ambrosia.loans.discord.check.CheckErrorList;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public class CheckDateRange extends CheckError<Instant> {

    private final Instant startDate;
    private final Instant endDate;

    public CheckDateRange(@Nullable Instant startDate, @Nullable Instant endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void checkAll(Instant value, CheckErrorList error) {
        if (startDate != null && value.isBefore(startDate))
            error.addError("Date must be before '%s'".formatted(formatDate(startDate)));
        if (endDate != null && value.isAfter(endDate))
            error.addError("Date must be after '%s'".formatted(formatDate(endDate)));
    }
}
