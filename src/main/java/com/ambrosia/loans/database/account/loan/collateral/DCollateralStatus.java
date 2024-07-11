package com.ambrosia.loans.database.account.loan.collateral;

import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;

public enum DCollateralStatus {
    COLLECTED,
    RETURNED,
    SOLD;

    @DbEnumValue
    public String id() {
        return name();
    }

    @Override
    public String toString() {
        return Pretty.spaceEnumWords(name());
    }
}
