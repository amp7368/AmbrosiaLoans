package com.ambrosia.loans.database.account.event.loan;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasDateRange {

    @NotNull
    Instant getStartDate();

    @Nullable
    Instant getEndDate();

    @NotNull
    default Instant getEndDate(@NotNull Instant defaultIfNull) {
        return Objects.requireNonNullElse(getEndDate(), defaultIfNull);
    }

    @NotNull
    default Instant getEndDateOrNow() {
        return getEndDate(Instant.now());
    }

    default Duration getDuration(Instant start, Instant end) {
        Instant startDate = this.getStartDate();
        Instant endDate = this.getEndDate(end);
        Instant constrainedStart = start.isAfter(startDate) ? start : startDate;
        Instant constrainedEnd = end.isBefore(endDate) ? end : endDate;
        return Duration.between(constrainedStart, constrainedEnd);
    }

    default Duration getTotalDuration() {
        return Duration.between(this.getStartDate(), this.getEndDate(Instant.now()));
    }
}
