package com.ambrosia.loans.discord.base.command.option;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface CommandOption<R> {

    CommandOption<DClient> CLIENT = multi("client", "Client associated with this action", OptionType.STRING,
        OptionMapping::getAsString, ClientQueryApi::findByName);
    CommandOption<Member> DISCORD = basic("discord", "The discord of the client", OptionType.MENTIONABLE,
        OptionMapping::getAsMember);
    CommandOption<String> PROFILE_NAME = basic("profile_name", "The display name of the client's profile", OptionType.STRING,
        OptionMapping::getAsString);
    CommandOption<String> MINECRAFT = basic("minecraft", "Your minecraft username", OptionType.STRING,
        OptionMapping::getAsString);
    CommandOption<String> DISPLAY_NAME = basic("display_name", "The name to display on the profile", OptionType.STRING,
        OptionMapping::getAsString);
    CommandOptionMulti<Long, DCFStoredGui<?>> REQUEST = multi("request_id", "The id of the loan", OptionType.INTEGER,
        OptionMapping::getAsLong, ActiveRequestDatabase.get()::getRequest);
    CommandOptionMulti<String, DClient> VOUCH = multi("vouch", "", OptionType.STRING,
        OptionMapping::getAsString, ClientQueryApi::findByName);

    static <V, R> CommandOptionMulti<V, R> multi(String name, String description, OptionType type,
        Function<OptionMapping, V> mapping1, Function<V, R> mapping2) {
        return new CommandOptionMulti<>(name, description, type, mapping1, mapping2);
    }

    private static <R> CommandOption<R> basic(String name, String description, OptionType type, Function<OptionMapping, R> getOption) {
        return new CommandOptionBasic<>(name, description, type, getOption);
    }

    R getOptional(SlashCommandInteractionEvent event, R fallback);

    default R getOptional(SlashCommandInteractionEvent event) {
        return this.getOptional(event, null);
    }

    default R getRequired(SlashCommandInteractionEvent event) {
        R result = getOptional(event);
        if (result == null) ErrorMessages.missingOption(getOptionName()).replyError(event);
        return result;
    }

    String getOptionName();

    default void addOption(SubcommandData command) {
        this.addOption(command, false);
    }

    default void addOption(SlashCommandData command) {
        this.addOption(command, false);
    }

    void addOption(SubcommandData command, boolean required);

    void addOption(SlashCommandData command, boolean required);
}
