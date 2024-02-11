package com.ambrosia.loans.discord.check;

public enum CheckErrorLevel {
    FATAL,
    ERROR,
    WARNING,
    INFO;

    public boolean isError() {
        return this == FATAL || this == ERROR;
    }

    public boolean isWarning() {
        return this != INFO;
    }
}
