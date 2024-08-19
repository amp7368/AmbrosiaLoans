package com.ambrosia.loans;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public class Bank {

    public static final Duration INTEREST_INTERVAL = Duration.ofDays(7);
    public static final BigDecimal INTEREST_INTERVAL_DECIMAL = BigDecimal.valueOf(INTEREST_INTERVAL.toHours());
    public static final BigDecimal INVESTOR_SHARE = BigDecimal.valueOf(0.6);
    // 06/30/2024 00h:31m:03s GMT
    public static final Instant MIGRATION_DATE = Instant.ofEpochSecond(1719089272L);

    private static final int PRECISION = MathContext.DECIMAL128.getPrecision() * 2;
    public static final MathContext FLOOR_CONTEXT = new MathContext(PRECISION, RoundingMode.FLOOR);


    public static BigDecimal interest(Duration duration, BigDecimal amount, BigDecimal rate) {
        BigDecimal durationHours = BigDecimal.valueOf(duration.toHours());
        BigDecimal time = durationHours.divide(INTEREST_INTERVAL_DECIMAL, Bank.FLOOR_CONTEXT);
        return amount.multiply(time.multiply(rate));
    }

    public static Duration interestDuration(long interest, long amount, double rate) {
        return interestDuration(BigDecimal.valueOf(interest), BigDecimal.valueOf(amount), BigDecimal.valueOf(rate));
    }

    public static Duration interestDuration(BigDecimal interest, BigDecimal amount, BigDecimal rate) {
        // I = PRT
        // I/PR = T
        BigDecimal interval = BigDecimal.valueOf(INTEREST_INTERVAL.toSeconds());
        BigDecimal durationSeconds = interest.multiply(interval)
            .divide(amount.multiply(rate, Bank.FLOOR_CONTEXT), Bank.FLOOR_CONTEXT);
        return Duration.ofSeconds(durationSeconds.longValue());
    }

    public static Duration legacySimpleWeeksDuration(Duration duration) {
        double weeks = duration.toSeconds() / (double) Duration.ofDays(7).toSeconds();
        int up = (int) Math.ceil(weeks - 0.25);
        return Duration.ofDays(up * 7L);
    }
}
