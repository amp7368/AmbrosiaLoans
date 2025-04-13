package com.ambrosia.loans.discord.command.staff.calculator.interest.base;

import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public interface InterestMultiplierChoice {

    static InterestMultiplierChoiceData wrap(String label, double multiplier) {
        return new InterestMultiplierChoiceData(label, multiplier);
    }

    InterestMultiplierChoiceData data();

    default double multiplier() {
        return data().multiplier();
    }

    default String label() {
        return data().label();
    }

    String name();

    default SelectOption toSelectOption() {
        String label = "%s [%.2f]".formatted(label(), multiplier());
        return SelectOption.of(label, name()).withEmoji(color().getEmoji());
    }

    default AmbrosiaEmoji color() {
        return data().color();
    }

    default void setColor(AmbrosiaEmoji color) {
        data().setColor(color);
    }
}
