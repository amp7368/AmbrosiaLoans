package com.ambrosia.loans.database.message;


import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;

public enum MessageReason {
    LOAN_REMINDER,
    INVESTMENT_UPDATE,
    LOAN_FREEZE;

    @DbEnumValue
    public String getDBValue() {
        return name();
    }

    public String display() {
        return Pretty.spaceEnumWords(name());
    }
}
