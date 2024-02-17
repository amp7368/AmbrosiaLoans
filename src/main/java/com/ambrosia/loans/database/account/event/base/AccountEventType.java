package com.ambrosia.loans.database.account.event.base;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import io.ebean.annotation.DbEnumValue;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

public enum AccountEventType {
    // in order of transaction
    LOAN(0, AmbrosiaEmoji.LOAN_BALANCE),
    INTEREST(4, AmbrosiaEmoji.LOAN_INTEREST),
    ADJUST_LOAN(8, AmbrosiaEmoji.CHECK_ERROR),
    PAYMENT(2, AmbrosiaEmoji.LOAN_PAYMENT),
    INVEST(1, AmbrosiaEmoji.INVESTMENT_BALANCE),
    ADJUST_DOWN(6, AmbrosiaEmoji.CHECK_ERROR),
    ADJUST_UP(7, AmbrosiaEmoji.CHECK_ERROR),
    PROFIT(5, AmbrosiaEmoji.INVESTMENT_PROFITS),
    WITHDRAWAL(3, AmbrosiaEmoji.ANY_WITHDRAWAL),
    TRADE_GIVE(9, AmbrosiaEmoji.TRADE),
    TRADE_GET(10, AmbrosiaEmoji.TRADE);

    public static final String DEFINITION = "event_type";
    public static final Comparator<? super AccountEventType> ORDER = Comparator.comparing(Function.identity());
    private static final Set<AccountEventType> LOAN_LIKE = Set.of(LOAN, INTEREST, PAYMENT, ADJUST_LOAN);
    private final int id;
    private final AmbrosiaEmoji emoji;

    AccountEventType(int id, AmbrosiaEmoji emoji) {
        this.id = id;
        this.emoji = emoji;
    }

    @DbEnumValue(withConstraint = false)
    public String getDBValue() {
        return name();
    }

    @Override
    public String toString() {
        return Pretty.spaceEnumWords(this.name());
    }

    public boolean isLoanLike() {
        return LOAN_LIKE.contains(this);
    }

    public boolean isProfit() {
        return this == PROFIT || this == ADJUST_UP;
    }

    public AmbrosiaEmoji getEmoji() {
        return this.emoji;
    }
}
