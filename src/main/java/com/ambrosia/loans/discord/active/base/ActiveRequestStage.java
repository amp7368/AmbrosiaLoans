package com.ambrosia.loans.discord.active.base;

import com.ambrosia.loans.discord.base.AmbrosiaColor;
import com.ambrosia.loans.discord.base.AmbrosiaColor.AmbrosiaColorRequest;

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
        return this == COMPLETED;
    }
}
