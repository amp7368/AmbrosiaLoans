package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import java.util.Locale;

public enum ActiveRequestStage {
    DENIED(AmbrosiaColor.RED),
    CLAIMED(AmbrosiaColor.YELLOW),
    APPROVED(AmbrosiaColor.BLUE_NORMAL),
    COMPLETED(AmbrosiaColor.GREEN),
    UNCLAIMED(AmbrosiaColor.BLACK),
    ERROR(AmbrosiaColor.RED),
    CREATED(AmbrosiaColor.BLUE_SPECIAL);

    private final int color;

    ActiveRequestStage(int color) {
        this.color = color;
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
}
