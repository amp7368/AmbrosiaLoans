package com.ambrosia.loans.util.clover;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.util.AmbrosiaTimeZone;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;

public record CloverRequest(UUID player, Instant start, int termsAfter, CloverTimeResolution timeResolution) {

    public CloverRequest(UUID player, CloverTimeResolution timeResolution) {
        this(player, timeResolution.past(), timeResolution.termsAfter(), timeResolution);
    }

    public enum CloverTimeResolution {
        DAY,
        WEEK;

        public Instant past() {
            ZonedDateTime today = LocalDateTime.now().atZone(AmbrosiaTimeZone.getTimeZoneId());
            ZonedDateTime past = switch (this) {
                case DAY -> today.minusDays(termsAfter());
                case WEEK -> today.minusWeeks(termsAfter());
            };
            return Instant.from(past);
        }

        public int termsAfter() {
            return switch (this) {
                case DAY -> 60;
                case WEEK -> 52;
            };
        }

        public String display() {
            return Pretty.spaceEnumWords(name());
        }

        public double upperBounds() {
            return switch (this) {
                case DAY -> 40 / 7d;
                case WEEK -> 40;
            };
        }

        public RegularTimePeriod graphUnit(Instant retrieved) {
            Date date = Date.from(retrieved);
            return switch (this) {
                case DAY -> new Day(date);
                case WEEK -> new Week(date);
            };
        }

        public RegularTimePeriod graphPast() {
            return graphUnit(past());
        }
    }
}
