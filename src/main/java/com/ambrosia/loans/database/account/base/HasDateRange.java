package com.ambrosia.loans.database.account.base;

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
    default Instant getEarliestOfEnd(Instant otherEnd) {
        Instant endDate = getEndDate();
        if (endDate == null) return otherEnd;
        if (endDate.isAfter(otherEnd)) return otherEnd;
        return endDate;
    }

    @NotNull
    default Instant getEndDateOrNow() {
        return getEndDate(Instant.now());
    }

    default Duration getDuration(Instant start, Instant end) {
        Instant startDate = this.getStartDate();
        Instant constrainedStart = start.isAfter(startDate) ? start : startDate;
        Instant constrainedEnd = this.getEarliestOfEnd(end);
        return Duration.between(constrainedStart, constrainedEnd);
    }

    default boolean isDateDuring(Instant date) {
        Instant startDate = this.getStartDate();
        if (startDate.isAfter(date)) return false;
        Instant endDate = this.getEndDate();
        if (endDate == null) return true;
        return date.isBefore(endDate);
    }

    default Duration getTotalDuration() {
        return Duration.between(this.getStartDate(), this.getEndDate(Instant.now()));
    }
}
