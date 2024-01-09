package com.ambrosia.loans.database.loan;

import io.ebean.annotation.DbEnumValue;

public enum DLoanStatus {
    ACTIVE,
    FROZEN,
    PAID,
    DEFAULTED;

    @DbEnumValue
    public String id() {
        // todo change to integer
        return name();
    }
}
