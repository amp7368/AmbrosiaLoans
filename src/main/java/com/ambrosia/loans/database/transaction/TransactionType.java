package com.ambrosia.loans.database.transaction;

import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;

public enum TransactionType {
    LOAN(0),
    INVEST(1),
    PROFIT(2),
    INTEREST(3),
    TRADE_GIVE(4),
    TRADE_GET(5);
    private final int id;

    TransactionType(int id) {
        this.id = id;
    }

    @DbEnumValue
    public int getId() {
        return id;
    }

    public String displayName() {
        return Pretty.upperCaseFirst(this.name());
    }
}
