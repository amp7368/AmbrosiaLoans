package com.ambrosia.loans.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class AmbrosiaTimeZone {

    public static final ZoneId TIME_ZONE_ID = ZoneId.of("America/Los_Angeles");
    public static final DateTimeFormatter SIMPLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("MM/dd/yy")
        .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
        .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
        .toFormatter()
        .withZone(getTimeZoneId());

    public static ZoneId getTimeZoneId() {
        return TIME_ZONE_ID;
    }


    public static Instant parse(String dateString) throws DateTimeParseException {
        TemporalAccessor parsed = SIMPLE_DATE_FORMATTER.parse(dateString);
        return Instant.from(parsed);
    }

    public static String formatSimple(Instant date) {
        return SIMPLE_DATE_FORMATTER.format(date);
    }
}
