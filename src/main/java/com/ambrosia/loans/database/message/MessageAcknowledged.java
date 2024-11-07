package com.ambrosia.loans.database.message;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;

public enum MessageAcknowledged {
    SENDING,
    SENT,
    ACKNOWLEDGED,
    ERROR;

    public String display() {
        return Pretty.spaceEnumWords(name());
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
