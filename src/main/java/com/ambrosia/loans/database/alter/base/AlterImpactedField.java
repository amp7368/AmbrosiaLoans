package com.ambrosia.loans.database.alter.base;

import java.util.Collection;
import java.util.List;

public enum AlterImpactedField {
    INVESTMENT_AMOUNT,
    INVESTMENT_DATE,
    LOAN_INITIAL_AMOUNT,
    LOAN_RATE,
    LOAN_START_DATE,
    CLIENT_BLACKLISTED,
    LOAN_END_DATE;

    public static Collection<AlterImpactedField> allClient() {
        return List.of(CLIENT_BLACKLISTED);
    }
}
