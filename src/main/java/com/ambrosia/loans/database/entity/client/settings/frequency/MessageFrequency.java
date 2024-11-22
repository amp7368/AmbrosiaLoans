package com.ambrosia.loans.database.entity.client.settings.frequency;

import com.ambrosia.loans.util.AmbrosiaTimeZone;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageFrequency {

    public MessageFrequencyUnit unit = MessageFrequencyUnit.DEFAULT;
    public int frequencyAmount = -1;
    public Date startDate;

    public MessageFrequency() {
    }

    private MessageFrequency(MessageFrequencyUnit unit, int frequencyAmount, Instant startDate) {
        this.unit = unit;
        this.frequencyAmount = frequencyAmount;
        if (startDate != null) this.startDate = Date.from(startDate);
    }

    public static MessageFrequency createDefault(@NotNull MessageFrequencyUnit unit, int frequencyAmount) {
        if (unit == MessageFrequencyUnit.DEFAULT)
            throw new IllegalArgumentException("Cannot set a default MessageFrequency with DEFAULT units");
        if (frequencyAmount <= 0)
            throw new IllegalArgumentException(
                "Must set MessageFrequency$frequencyAmount to a positive number so there are no infinite messages");
        return new MessageFrequency(unit, frequencyAmount, null);
    }

    public static MessageFrequency createNever() {
        return new MessageFrequency(MessageFrequencyUnit.NEVER, 1, null);
    }

    @Override
    public String toString() {
        return "MessageFrequency{" +
            "unit=" + unit +
            ", frequencyAmount=" + frequencyAmount +
            ", startDate=" + startDate +
            '}';
    }

    private Instant getStartDate() {
        return this.startDate.toInstant();
    }

    @Nullable
    public NextMessageTime calculate(@Nullable Instant startDate, Instant current, MessageFrequency defaultDuration) {
        if (startDate == null) startDate = current;
        else if (this.startDate != null) startDate = this.getStartDate();

        Instant first = addTo(startDate, current, defaultDuration);
        if (first == null) return null;

        Instant now = Instant.now();
        if (first.isBefore(now)) first = now;

        Instant second = addTo(first, first, defaultDuration);
        if (second == null) return null;
        String display = display(defaultDuration);
        return new NextMessageTime(first, second, display);
    }

    @Nullable
    public Instant addTo(@NotNull Instant startDate, @NotNull Instant current, MessageFrequency defaultDuration) {
        TemporalUnit timeUnit = unit.getUnit();
        if (timeUnit == null) {
            if (defaultDuration == null) throw new IllegalArgumentException("Default duration used DEFAULT units");
            return defaultDuration.addTo(startDate, current, null);
        }
        if (timeUnit == ChronoUnit.FOREVER) return null;

        ZonedDateTime startDateLocal = ZonedDateTime.ofInstant(startDate, AmbrosiaTimeZone.getTimeZoneId());
        ZonedDateTime currentLocal = ZonedDateTime.ofInstant(current, AmbrosiaTimeZone.getTimeZoneId());

        long between = timeUnit.between(startDateLocal, currentLocal) / frequencyAmount;
        long nextDateAmount = (between + 1) * frequencyAmount;

        return startDateLocal.plus(nextDateAmount, timeUnit).toInstant();
    }

    public String display(@Nullable MessageFrequency ifDefault) {
        String ifDefaultStr = ifDefault == null ? null : ifDefault.display(null);
        return unit.display(frequencyAmount, ifDefaultStr);
    }
}
