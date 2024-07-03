package com.ambrosia.loans.discord.misc.autocomplete;

import com.ambrosia.loans.database.entity.client.ClientSearch;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;

public class ClientAutoComplete extends AmbrosiaAutoComplete<DClient> {

    public ClientAutoComplete(String optionName) {
        super(optionName);
    }

    @Override
    protected List<DClient> autoComplete(@NotNull CommandAutoCompleteInteractionEvent event, String arg) {
        if (!event.isFromGuild()) return List.of();

        boolean isEmployee = DiscordPermissions.get().isEmployee(event.getMember());
        if (!isEmployee) return List.of();

        return ClientSearch.autoComplete(arg);
    }

    @NotNull
    @Override
    protected Choice choice(DClient client) {
        return new Choice(client.getEffectiveName(), client.getEffectiveName());
    }
}
