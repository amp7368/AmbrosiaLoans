package com.ambrosia.loans.discord.base.command.option;

import java.util.function.Function;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandOptionBasic<R> implements CommandOption<R> {

    protected final String description;
    protected final String name;
    protected final OptionType type;
    protected final Function<OptionMapping, R> getOption;

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
    public R getOptional(SlashCommandInteractionEvent event, R fallback) {
        return event.getOption(name, fallback, this.getOption);
    }

    @Override
    public void addOption(SubcommandData command, boolean required) {
        command.addOption(type, name, description, required);
    }

    @Override
    public void addOption(SlashCommandData command, boolean required) {
        command.addOption(type, name, description, required);
    }

}
