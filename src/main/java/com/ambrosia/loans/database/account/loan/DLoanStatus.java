package com.ambrosia.loans.database.account.loan;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import io.ebean.annotation.DbEnumValue;

public enum DLoanStatus {
    ACTIVE(AmbrosiaEmoji.STATUS_ACTIVE),
    FROZEN(AmbrosiaEmoji.STATUS_PENDING),
    PAID(AmbrosiaEmoji.STATUS_COMPLETE),
    DEFAULTED(AmbrosiaEmoji.STATUS_ERROR);

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
