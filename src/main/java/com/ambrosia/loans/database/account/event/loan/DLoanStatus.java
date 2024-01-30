package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.discord.DiscordBot;
import io.ebean.annotation.DbEnumValue;

public enum DLoanStatus {
    ACTIVE(1201322960800194631L),
    FROZEN(1201322956635242516L),
    PAID(1201322956635242516L),
    DEFAULTED(1201322930945151017L);

    private final long emojiId;
    private transient String emoji;

    DLoanStatus(long emojiId) {
        this.emojiId = emojiId;
    }

    @DbEnumValue
    public String id() {
        return name();
    }


    public String getEmoji() {
        if (this.emoji != null) return this.emoji;
        return this.emoji = DiscordBot.dcf.jda()
            .getEmojiById(emojiId)
            .getFormatted();
    }

    public boolean isActive() {
        return this == ACTIVE || this == FROZEN;
    }
}
