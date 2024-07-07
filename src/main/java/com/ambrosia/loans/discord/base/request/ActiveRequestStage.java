package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.Locale;

public enum ActiveRequestStage {
    DENIED(AmbrosiaColor.RED, AmbrosiaEmoji.STATUS_ERROR),
    CLAIMED(AmbrosiaColor.YELLOW, AmbrosiaEmoji.STATUS_PENDING),
    APPROVED(AmbrosiaColor.BLUE_NORMAL, AmbrosiaEmoji.STATUS_ACTIVE),
    COMPLETED(AmbrosiaColor.GREEN, AmbrosiaEmoji.STATUS_COMPLETE),
    UNCLAIMED(AmbrosiaColor.BLACK, AmbrosiaEmoji.STATUS_OFFLINE),
    ERROR(AmbrosiaColor.RED, AmbrosiaEmoji.STATUS_ERROR),
    CREATED(AmbrosiaColor.BLUE_SPECIAL, AmbrosiaEmoji.STATUS_OFFLINE);

    private final int color;
    private final AmbrosiaEmoji emoji;

    ActiveRequestStage(int color, AmbrosiaEmoji emoji) {
        this.color = color;
        this.emoji = emoji;
    }

    public int getColor() {
        return color;
    }

    public boolean isComplete() {
        return this == COMPLETED || this == DENIED;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public AmbrosiaEmoji getEmoji() {
        return this.emoji;
    }

    public boolean isBeforeClaimed() {
        return this == CREATED || this == UNCLAIMED;
    }
}
