package com.ambrosia.loans.discord.autocomplete;

import com.ambrosia.loans.database.entity.client.ClientSearch;
import com.ambrosia.loans.database.entity.client.DClient;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;

public class VouchAutoComplete extends AmbrosiaAutoComplete<DClient> {

    public VouchAutoComplete() {
        super("vouch");
    }

    @Override
    protected List<DClient> autoComplete(@NotNull CommandAutoCompleteInteractionEvent event, String arg) {
        return ClientSearch.findByNamePartial(arg);
    }

    @NotNull
    @Override
    protected Choice choice(DClient client) {
        return new Choice(client.getEffectiveName(), client.getEffectiveName());
    }
}
