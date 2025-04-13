package com.ambrosia.loans.discord.command.staff.calculator.interest.base;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;

public final class InterestMultiplierChoiceData {

    private final String label;
    private final double multiplier;
    private AmbrosiaEmoji color;

    public InterestMultiplierChoiceData(String label, double multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }

    public InterestMultiplierChoiceData setColor(AmbrosiaEmoji color) {
        this.color = color;
        return this;
    }

    public AmbrosiaEmoji color() {
        return color;
    }

    public String label() {
        return label;
    }

    public double multiplier() {
        return multiplier;
    }
}
