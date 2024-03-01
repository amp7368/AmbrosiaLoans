package com.ambrosia.loans.discord.misc.autocomplete;

import com.ambrosia.loans.discord.DiscordModule;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;

public abstract class AmbrosiaAutoComplete<T> {

    private final String optionName;

    public AmbrosiaAutoComplete(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionName() {
        return optionName;
    }

    protected abstract Choice choice(T obj);

    public final void autoCompleteChoices(@NotNull CommandAutoCompleteInteractionEvent event, String arg) {
        List<T> autoCompleted = autoComplete(event, arg);
        int maxSize = Math.min(autoCompleted.size(), DiscordModule.MAX_CHOICES);
        List<Choice> choices = autoCompleted
            .subList(0, maxSize)
            .stream()
            .map(this::choice)
            .toList();
        event.replyChoices(choices).queue();
    }

    protected abstract List<T> autoComplete(@NotNull CommandAutoCompleteInteractionEvent event, String arg);

}
