package com.ambrosia.loans.discord.autocomplete;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;

public abstract class AmbrosiaAutoComplete {

    private final String optionName;

    public AmbrosiaAutoComplete(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionName() {
        return optionName;
    }

    public abstract void autoComplete(@NotNull CommandAutoCompleteInteractionEvent event, String arg);

}
