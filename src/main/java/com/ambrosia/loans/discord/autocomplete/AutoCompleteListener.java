package com.ambrosia.loans.discord.autocomplete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class AutoCompleteListener extends ListenerAdapter {


    private final Map<String, AmbrosiaAutoComplete<?>> autoCompletes = new HashMap<>();

    public AutoCompleteListener() {
        getAutoCompletes().forEach(auto -> autoCompletes.put(auto.getOptionName(), auto));
    }

    @NotNull
    private List<AmbrosiaAutoComplete<?>> getAutoCompletes() {
        return List.of(new VouchAutoComplete());
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
