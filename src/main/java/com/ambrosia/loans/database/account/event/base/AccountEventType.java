package com.ambrosia.loans.database.account.event.base;

import apple.utilities.util.Pretty;
import io.ebean.annotation.DbEnumValue;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

public enum AccountEventType {
    // in order of transaction
    LOAN(0),
    INTEREST(4),
    ADJUST_LOAN(8),
    PAYMENT(2),
    INVEST(1),
    ADJUST_DOWN(6),
    ADJUST_UP(7),
    PROFIT(5),
    WITHDRAWAL(3),
    TRADE_GIVE(9),
    TRADE_GET(10);

    public static final Set<AccountEventType> INVEST_LIKE = Set.of(INVEST, WITHDRAWAL, ADJUST_DOWN, ADJUST_UP);
    public static final Comparator<? super AccountEventType> ORDER = Comparator.comparing(Function.identity());
    private static final Set<AccountEventType> LOAN_LIKE = Set.of(LOAN, PAYMENT, ADJUST_UP);
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

    public boolean isLoanLike() {
        return LOAN_LIKE.contains(this);
    }
}
