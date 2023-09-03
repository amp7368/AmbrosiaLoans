package com.ambrosia.loans.bank;

import apple.lib.modules.AppleModule;

import java.time.Duration;

public class Bank extends AppleModule {

    public static final Duration INTEREST_RATE_UNITS = Duration.ofDays(7);
    public static final Duration INTEREST_ACCUMULATE_INTERVAL = Duration.ofDays(7);
    public static final Duration LOAN_GRACE_PERIOD = Duration.ofDays(7);
    public static final double INTEREST_RATE_MODIFIER =
            INTEREST_RATE_UNITS.toMillis() / (double) INTEREST_ACCUMULATE_INTERVAL.toMillis();

    @Override
    public void onEnable() {
    }

    @Override
    public String getName() {
        return "Bank";
    }
}
