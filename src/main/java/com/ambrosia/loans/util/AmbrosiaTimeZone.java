package com.ambrosia.loans.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;

public class AmbrosiaTimeZone {

    private static final ZoneId TIME_ZONE_ID = ZoneId.of("America/Los_Angeles");
    private static final DateTimeFormatter DATE_FORMATTER = createDateFormatters("MM/dd/yy").get(0);
    private static final List<DateTimeFormatter> DATE_PARSERS = new ArrayList<>();
    private static final ReadWriteLock YEAR_WRITE_LOCK = new ReentrantReadWriteLock();
    private static Instant yearReset = Instant.MIN;

    static {
        initializeDateFormatters();
    }

    private static void initializeDateFormatters() {
        YEAR_WRITE_LOCK.writeLock().lock();
        // update
        DATE_PARSERS.clear();
        DATE_PARSERS.addAll(createDateFormatters(
            "MM/dd[/yy[ HH:mm[:ss]]]",
            "MM/dd[/yyyy[ HH:mm[:ss]]]",
            "MM-dd[-yy[ HH:mm[:ss]]]",
            "MM-dd[-yyyy[ HH:mm[:ss]]]",
            "MM_dd[_yy[ HH:mm[:ss]]]",
            "MM_dd[_yyyy[ HH:mm[:ss]]]"
        ));
        yearReset = LocalDateTime.now(TIME_ZONE_ID)
            .plusYears(1)
            .withDayOfYear(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .toInstant(ZoneOffset.ofHours(0));
        YEAR_WRITE_LOCK.writeLock().unlock();
    }

    private static @NotNull List<DateTimeFormatter> createDateFormatters(String... patterns) {
        return Arrays.stream(patterns)
            .map(pattern -> new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter()
                .withZone(getTimeZoneId())
            ).toList();
    }

    public static ZoneId getTimeZoneId() {
        return TIME_ZONE_ID;
    }


    public static Instant parse(String dateString) throws DateTimeParseException {
        boolean needsUpdate = Instant.now().isBefore(yearReset);
        if (!needsUpdate) {
            YEAR_WRITE_LOCK.writeLock().lock();
            try {
                initializeDateFormatters();
            } finally {
                YEAR_WRITE_LOCK.writeLock().unlock();
            }
        }
        YEAR_WRITE_LOCK.readLock().lock();
        try {
            return readLockedParse(dateString);
        } finally {
            YEAR_WRITE_LOCK.readLock().unlock();
        }
    }

    private static Instant readLockedParse(String dateString) {
        DateTimeParseException firstException = null;
        for (int i = 0; i < DATE_PARSERS.size(); i++) {
            try {
                TemporalAccessor parsed = DATE_PARSERS.get(i).parse(dateString);
                return Instant.from(parsed);
            } catch (DateTimeParseException e) {
                if (i == 0) firstException = e;
            }
        }
        if (firstException == null) throw new RuntimeException("DATE_PARSERS is empty");
        throw firstException;
    }

    public static String formatSimple(Instant date) {
        return DATE_FORMATTER.format(date);
    }
}
