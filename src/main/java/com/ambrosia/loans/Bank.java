package com.ambrosia.loans;

import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;

public class Bank {

    public static final Duration INTEREST_INTERVAL = Duration.ofDays(7);
    public static final Duration INTEREST_ACCUMULATE_INTERVAL = Duration.ofDays(7);
    public static final BigDecimal INTEREST_INTERVAL_DECIMAL = BigDecimal.valueOf(INTEREST_INTERVAL.toHours());
    public static final BigDecimal INVESTOR_SHARE = BigDecimal.valueOf(0.6);
    public static final Duration LOAN_GRACE_PERIOD = Duration.ofDays(7);
    public static final double INTEREST_RATE_MODIFIER =
        INTEREST_INTERVAL.toMillis() / (double) INTEREST_ACCUMULATE_INTERVAL.toMillis();

    public static BigDecimal interest(Duration duration, BigDecimal amount, BigDecimal rate) {
        BigDecimal durationHours = BigDecimal.valueOf(duration.toHours());
        BigDecimal interval = BigDecimal.valueOf(Bank.INTEREST_INTERVAL.toHours());
        BigDecimal time = durationHours.divide(interval, MathContext.DECIMAL128);
        BigDecimal exponent = time.multiply(rate);
        BigDecimal multiplier = BigDecimalMath.pow(BigDecimal.valueOf(Math.E), exponent, MathContext.DECIMAL128);
        return amount.multiply(multiplier);
    }
}
