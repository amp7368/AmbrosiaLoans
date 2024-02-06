package com.ambrosia.loans.migrate.investment;

public enum RawInvestmentType {
    INVEST,
    CONFIRM,
    WITHDRAWAL,
    CLOSED;

    public boolean isConfirm() {
        return this == CONFIRM || this == CLOSED;
    }
}
