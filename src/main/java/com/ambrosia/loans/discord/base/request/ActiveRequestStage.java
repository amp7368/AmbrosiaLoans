package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor.AmbrosiaColorRequest;
import java.util.Locale;

public enum ActiveRequestStage {
    DENIED(AmbrosiaColor.BAD),
    CLAIMED(AmbrosiaColorRequest.CLAIMED),
    APPROVED(AmbrosiaColorRequest.CLAIMED),
    COMPLETED(AmbrosiaColor.SUCCESS),
    UNCLAIMED(AmbrosiaColorRequest.UNCLAIMED),
    ERROR(AmbrosiaColor.ERROR),
    CREATED(AmbrosiaColor.NORMAL);

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
