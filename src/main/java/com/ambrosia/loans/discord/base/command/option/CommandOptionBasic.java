package com.ambrosia.loans.discord.base.command.option;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandOptionBasic<R> implements CommandOption<R> {

    protected final String description;
    protected final String name;
    protected final OptionType type;
    protected final Function<OptionMapping, R> getOption;
    private boolean autoComplete = false;
    private List<Choice> choices;

    CommandOptionBasic(String name, String description, OptionType type, Function<OptionMapping, R> getOption) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.getOption = getOption;
    }


    @Override
    public String getOptionName() {
        return this.name;
    }

    @Override
    public R getOptional(CommandInteraction event, R fallback) {
        return event.getOption(name, fallback, this.getOption);
    }

    @Override
    public void addOption(SubcommandData command, boolean required) {
        command.addOption(type, name, description, required, autoComplete);
    }

    @Override
    public void addOption(SlashCommandData command, boolean required) {
        command.addOptions(createOption(required));
    }

    public OptionData createOption(boolean required) {
        OptionData optionData = new OptionData(type, name, description, required, autoComplete);
        if (choices != null) optionData.addChoices(this.choices);
        return optionData;
    }

    public CommandOptionBasic<R> addChoices(List<Choice> choices) {
        if (this.choices == null) this.choices = new ArrayList<>();
        this.choices.addAll(choices);
        return this;
    }

    public CommandOptionBasic<R> setAutocomplete() {
        this.autoComplete = true;
        return this;
    }
}
