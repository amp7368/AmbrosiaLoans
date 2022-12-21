package com.ambrosia.loans.discord.base;

import java.util.function.Function;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public enum CommandOption implements SendMessage {
    CLIENT("client", "Client associated with this action", OptionType.STRING, OptionMapping::getAsString),
    DISCORD_OPTION("discord", "The discord of the client", OptionType.STRING, OptionMapping::getAsMember),
    PROFILE_NAME("profile_name", "The display name of the client's profile", OptionType.STRING, OptionMapping::getAsString),
    MINECRAFT("minecraft", "Your minecraft username", OptionType.STRING, OptionMapping::getAsString),
    DISPLAY_NAME("display_name", "The name to display on the profile", OptionType.STRING, OptionMapping::getAsString);

    private final String description;
    private final String name;
    private final OptionType type;
    private final Function<? super OptionMapping, ?> getOption;

    CommandOption(String name, String description, OptionType type, Function<? super OptionMapping, ?> getOption) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.getOption = getOption;
    }

    public <T> T getOptional(SlashCommandInteractionEvent event) {
        @SuppressWarnings("unchecked") T result = (T) event.getOption(name, this.getOption);
        return result;
    }

    public <T> T getOptional(SlashCommandInteractionEvent event, T fallback) {
        @SuppressWarnings("unchecked") T result = (T) event.getOption(name, fallback, this.getOption);
        return result;
    }

    public <T> T getRequired(SlashCommandInteractionEvent event) {
        T result = getOptional(event);
        if (result == null) missingOption(event, name);
        return result;
    }

    public void addOption(SubcommandData command) {
        this.addOption(command, false);
    }

    public void addOption(SlashCommandData command) {
        this.addOption(command, false);
    }

    public void addOption(SubcommandData command, boolean required) {
        command.addOption(type, name, description, required);
    }

    public void addOption(SlashCommandData command, boolean required) {
        command.addOption(type, name, description, required);
    }
}
