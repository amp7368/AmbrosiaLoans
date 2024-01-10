package com.ambrosia.loans.database.log.base;

import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;

public enum AccountEventType {
    LOAN(0),
    INVEST(1),
    INTEREST(2),
    PROFIT(3),
    TRADE_GIVE(4),
    TRADE_GET(5);
    private final int id;

    AccountEventType(int id) {
        this.id = id;
    }

    @DbEnumValue
    public int getId() {
        return id;
    }

    public String displayName() {
        return Pretty.spaceEnumWords(this.name());
    }
}
