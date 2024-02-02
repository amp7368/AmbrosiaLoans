package com.ambrosia.loans.database.account.event.base;

import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;
import java.util.Set;

public enum AccountEventType {
    LOAN(0),
    INVEST(1),
    PAYMENT(2),
    WITHDRAWAL(3),
    INTEREST(4),
    PROFIT(5),
    ADJUST_DOWN(6),
    ADJUST_UP(7),
    TRADE_GIVE(8),
    TRADE_GET(9);
    public static final Set<AccountEventType> INVEST_LIKE = Set.of(INVEST, WITHDRAWAL, ADJUST_DOWN, ADJUST_UP);
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

    public boolean isInvestLike() {
        return INVEST_LIKE.contains(this);
    }
}
