package com.ambrosia.loans.discord.base.exception;

import java.time.Instant;

public class BadDateAccessException extends Exception {

    private final Instant date;

    public BadDateAccessException(Instant date) {
        this.date = date;
    }

    public Instant getDate() {
        return this.date;
    }
}
