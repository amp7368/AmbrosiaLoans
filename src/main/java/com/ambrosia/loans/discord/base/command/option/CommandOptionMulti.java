package com.ambrosia.loans.discord.base.command.option;

import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import java.util.List;
import java.util.function.Function;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
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

    @Override
    public AmbrosiaMessage getErrorMessage(CommandInteraction event) {
        V mapped = getMap1(event);
        if (mapped == null) return super.getErrorMessage(event);
        return ErrorMessages.invalidOption(getOptionName(), mapped);
    }

    @Override
    public CommandOptionMulti<V, R> setAutocomplete() {
        super.setAutocomplete();
        return this;
    }

    @Override
    public CommandOptionBasic<R> addChoices(List<Choice> choices) {
        super.addChoices(choices);
        return this;
    }

    @Override
    public CommandOptionBasic<R> addChoices(String... choices) {
        super.addChoices(choices);
        return this;
    }

    public V getMap1(CommandInteraction event) {
        return event.getOption(this.name, this.mapping1);
    }
}
