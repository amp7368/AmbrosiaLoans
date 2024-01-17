package com.ambrosia.loans.discord.base.command.option;

import com.ambrosia.loans.database.entity.client.ClientApi;
import com.ambrosia.loans.database.entity.client.DClient;
import java.util.function.Function;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

public class CommandOptionMulti<V, R> extends CommandOptionBasic<R> {


    private final Function<OptionMapping, V> mapping1;

    CommandOptionMulti(String name, String description, OptionType type,
        Function<OptionMapping, V> mapping1, Function<V, R> mapping2) {
        super(name, description, type, composed(mapping1, mapping2));
        this.mapping1 = mapping1;
    }

    @NotNull
    private static <V, R> Function<OptionMapping, R> composed(Function<OptionMapping, V> mapping1, Function<V, R> mapping2) {
        return mapping1.andThen(v -> {
            if (v == null) return null;
            return mapping2.apply(v);
        });
    }

    public static DClient findClient(SlashCommandInteractionEvent event) {
        return findClientApi(event).entity;
    }

    public static ClientApi findClientApi(SlashCommandInteractionEvent event) {
        return ClientApi.findByName(CommandOption.CLIENT.getRequired(event));
    }

    public V getOptionalMap1(SlashCommandInteractionEvent event) {
        return event.getOption(this.name, this.mapping1);
    }
}