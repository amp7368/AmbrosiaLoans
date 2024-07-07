package com.ambrosia.loans.database.account.loan.collateral;

import io.ebean.annotation.DbEnumValue;

public enum CollateralStatus {
    COLLECTED,
    RETURNED,
    SOLD;

    @DbEnumValue
    public String id() {
        return name();
    }
}
