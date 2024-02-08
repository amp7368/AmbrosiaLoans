package com.ambrosia.loans.discord.autocomplete;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordPermissions;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class AutoCompleteListener extends ListenerAdapter {


    private final Map<String, AmbrosiaAutoComplete<?>> autoCompletes = new HashMap<>();

    public AutoCompleteListener() {
        getAutoCompletes().forEach(auto -> autoCompletes.put(auto.getOptionName(), auto));
    }

    @NotNull
    private List<AmbrosiaAutoComplete<?>> getAutoCompletes() {
        return List.of(new ClientAutoComplete("vouch"), new ClientAutoComplete("client"));
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.getCommandString().equals("view_profile")) {
            SendMessage send = SendMessage.get();
            if (event.getMember() == null) {
                ErrorMessages.onlyInAmbrosia().replyError(event);
                return;
            }
            if (!DiscordPermissions.get().isEmployee(event.getMember())) {
                ErrorMessages.badRole("employee", event).replyError(event);
                return;
            }
            DClient client = ClientQueryApi.findByDiscord(event.getTarget().getIdLong());
            if (client == null) {
                send.replyError(event, "%s is not registered!".formatted(event.getTarget().getEffectiveName()));
                return;
            }
            client.profile(event::reply).send();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String option = event.getFocusedOption().getName();
        AmbrosiaAutoComplete<?> autoComplete;
        synchronized (autoCompletes) {
            autoComplete = autoCompletes.get(option);
        }
        if (autoComplete != null)
            autoComplete.autoCompleteChoices(event, event.getFocusedOption().getValue());
    }
}
