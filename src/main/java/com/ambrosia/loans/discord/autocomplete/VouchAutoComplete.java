package com.ambrosia.loans.discord.autocomplete;

import com.ambrosia.loans.database.entity.client.ClientSearch;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordModule;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;

public class VouchAutoComplete extends AmbrosiaAutoComplete {

    public VouchAutoComplete() {
        super("vouch");
    }

    @Override
    public void autoComplete(@NotNull CommandAutoCompleteInteractionEvent event, String arg) {
        List<DClient> clients = ClientSearch.findByNamePartial(arg);
        int maxSize = Math.min(clients.size(), DiscordModule.MAX_CHOICES);
        List<Choice> choices = clients
            .subList(0, maxSize)
            .stream()
            .map(this::choice)
            .toList();
        event.replyChoices(choices)
            .queue();
    }

    @NotNull
    private Choice choice(DClient client) {
        return new Choice(client.getEffectiveName(), client.getEffectiveName());
    }
}
