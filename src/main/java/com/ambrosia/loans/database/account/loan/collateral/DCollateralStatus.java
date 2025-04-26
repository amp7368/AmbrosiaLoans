package com.ambrosia.loans.database.account.loan.collateral;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import io.ebean.annotation.DbEnumValue;

public enum DCollateralStatus {
    NOT_COLLECTED,
    COLLECTED,
    RETURNED,
    SOLD,
    SOLD_FOR_PAYMENT,
    DEFAULTED,
    DELETED;

    public static DCollateralStatus[] commandChoices() {
        return new DCollateralStatus[]{COLLECTED, RETURNED, SOLD_FOR_PAYMENT, DEFAULTED};
    }

    public int getColor() {
        return switch (this) {
            case NOT_COLLECTED, COLLECTED, SOLD_FOR_PAYMENT -> AmbrosiaColor.GREEN;
            case RETURNED -> AmbrosiaColor.BLUE_NORMAL;
            case SOLD, DEFAULTED -> AmbrosiaColor.RED;
            case DELETED -> AmbrosiaColor.BLACK;
        };
    }

    @DbEnumValue
    public String id() {
        return name();
    }

    @Override
    public String toString() {
        return Pretty.spaceEnumWords(name());
    }

    public boolean requiresAmount() {
        return this == SOLD_FOR_PAYMENT || this == DEFAULTED;
    }
}
