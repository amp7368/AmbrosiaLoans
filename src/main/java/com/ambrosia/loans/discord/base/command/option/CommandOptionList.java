package com.ambrosia.loans.discord.base.command.option;

import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandOptionList {

    public List<CommandOption<?>> required;
    public List<CommandOption<?>> optional;

    private CommandOptionList(List<CommandOption<?>> required, List<CommandOption<?>> optional) {
        this.required = new ArrayList<>(required);
        this.optional = new ArrayList<>(optional);
    }

    public static CommandOptionList of(List<CommandOption<?>> required) {
        return of(required, List.of());
    }

    public static CommandOptionList of(List<CommandOption<?>> required, List<CommandOption<?>> optional) {
        return new CommandOptionList(required, optional);
    }

    public CommandOptionList addRequired(CommandOption<?>... options) {
        this.required.addAll(List.of(options));
        return this;
    }

    public CommandOptionList addOptional(CommandOption<?>... options) {
        this.optional.addAll(List.of(options));
        return this;
    }

    public SlashCommandData addToCommand(SlashCommandData command) {
        required.forEach(o -> o.addOption(command, true));
        optional.forEach(o -> o.addOption(command, false));
        return command;
    }

    public SubcommandData addToCommand(SubcommandData command) {
        required.forEach(o -> o.addOption(command, true));
        optional.forEach(o -> o.addOption(command, false));
        return command;
    }
}
