package com.ambrosia.loans.database.account.event.base;

import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;

public enum AccountEventType {
    LOAN(0),
    INVEST(1),
    PAYMENT(2),
    WITHDRAWAL(3),
    INTEREST(4),
    PROFIT(5),
    TRADE_GIVE(6),
    TRADE_GET(7);
    private final int id;

    AccountEventType(int id) {
        this.id = id;
    }

    @DbEnumValue
    public String getId() {
        return name();
    }

    @Override
    public String toString() {
        return Pretty.spaceEnumWords(this.name());
    }
}
