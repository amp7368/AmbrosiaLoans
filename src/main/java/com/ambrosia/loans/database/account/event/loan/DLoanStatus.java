package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import io.ebean.annotation.DbEnumValue;

public enum DLoanStatus {
    ACTIVE(AmbrosiaEmoji.LOAN_ACTIVE),
    FROZEN(AmbrosiaEmoji.LOAN_FROZEN),
    PAID(AmbrosiaEmoji.LOAN_PAID),
    DEFAULTED(AmbrosiaEmoji.LOAN_DEFAULTED);

    private final AmbrosiaEmoji emoji;

    DLoanStatus(AmbrosiaEmoji emoji) {
        this.emoji = emoji;
    }


    @DbEnumValue
    public String id() {
        return name();
    }


    public AmbrosiaEmoji getEmoji() {
        return this.emoji;
    }

    public boolean isActive() {
        return this == ACTIVE || this == FROZEN;
    }
}
