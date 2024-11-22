package com.ambrosia.loans.database.message;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import io.ebean.annotation.DbEnumValue;

public enum MessageAcknowledged {
    SENDING,
    SENT,
    ACKNOWLEDGED,
    ERROR;

    @DbEnumValue
    public String id() {
        return this.name();
    }

    public AmbrosiaEmoji getEmoji() {
        return switch (this) {
            case SENDING -> AmbrosiaEmoji.STATUS_OFFLINE;
            case SENT -> AmbrosiaEmoji.STATUS_PENDING;
            case ACKNOWLEDGED -> AmbrosiaEmoji.STATUS_COMPLETE;
            case ERROR -> AmbrosiaEmoji.STATUS_ERROR;
        };
    }

    public String display(boolean includeEmoji) {
        String displayName = Pretty.spaceEnumWords(name());
        if (includeEmoji)
            return getEmoji().spaced(displayName);
        return displayName;
    }

    public int getColor() {
        return switch (this) {
            case SENDING -> AmbrosiaColor.BLACK;
            case SENT -> AmbrosiaColor.YELLOW;
            case ACKNOWLEDGED -> AmbrosiaColor.GREEN;
            case ERROR -> AmbrosiaColor.RED;
        };
    }
}
