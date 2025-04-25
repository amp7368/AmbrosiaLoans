package com.ambrosia.loans.util.clover;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.util.AmbrosiaTimeZone;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;

public record CloverRequest(String player, Instant start, int termsAfter, CloverTimeResolution timeResolution) {

    public CloverRequest(String player, CloverTimeResolution timeResolution) {
        this(player, timeResolution.past(), timeResolution.termsAfter() + 1, timeResolution);
    }

    public enum CloverTimeResolution {
        DAY,
        WEEK,
        MONTH;

        public Instant past() {
            LocalDate date = switch (this) {
                case DAY:
                    yield LocalDate.now()
                        .minusDays(termsAfter());
                case WEEK:
                    yield LocalDate.now()
                        .minusWeeks(termsAfter())
                        .with(DayOfWeek.SUNDAY);
                case MONTH:
                    yield LocalDate.now()
                        .minusMonths(termsAfter())
                        .withDayOfMonth(1);
            };
            ZonedDateTime datetime = date.atTime(0, 0)
                .atZone(AmbrosiaTimeZone.getTimeZoneId());
            return Instant.from(datetime);
        }

        public int termsAfter() {
            return switch (this) {
                case DAY -> 60;
                case WEEK -> 52;
                case MONTH -> 12;
            };
        }

        public String display() {
            return Pretty.spaceEnumWords(name());
        }

        public double upperBounds() {
            return switch (this) {
                case DAY -> 8;
                case WEEK -> 40;
                case MONTH -> 100;
            };
        }

        public RegularTimePeriod graphUnit(Instant retrieved) {
            Date date = Date.from(retrieved);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            return switch (this) {
                case DAY -> new Day(date, calendar);
                case WEEK -> new Week(date, calendar);
                case MONTH -> new Month(date, calendar);
            };
        }

        public RegularTimePeriod graphPast() {
            return graphUnit(past());
        }

        public long countOfDays() {
            return switch (this) {
                case DAY -> 1;
                case WEEK -> 7;
                case MONTH -> 31;
            };
        }
    }
}
