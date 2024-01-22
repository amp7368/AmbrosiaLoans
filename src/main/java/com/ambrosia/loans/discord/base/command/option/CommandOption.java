package com.ambrosia.loans.discord.base.command.option;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
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

    // client
    CommandOptionMulti<String, DClient> CLIENT = multi("client", "Client associated with this action", OptionType.STRING,
        OptionMapping::getAsString, ClientQueryApi::findByName);
    CommandOption<Member> DISCORD = basic("discord", "The discord of the client", OptionType.MENTIONABLE,
        OptionMapping::getAsMember);
    CommandOption<String> MINECRAFT = basic("minecraft", "Your minecraft username", OptionType.STRING,
        OptionMapping::getAsString);
    CommandOption<String> DISPLAY_NAME = basic("display_name", "The name to display on the profile", OptionType.STRING,
        OptionMapping::getAsString);

    // request
    CommandOptionMulti<Long, DCFStoredGui<?>> REQUEST = multi("request_id", "The id of the loan", OptionType.INTEGER,
        OptionMapping::getAsLong, ActiveRequestDatabase.get()::getRequest);

    // loans
    CommandOptionMulti<String, DClient> VOUCH = multi("vouch", "Referral/vouch from someone with credit with Ambrosia",
        OptionType.STRING, OptionMapping::getAsString, ClientQueryApi::findByName);
    CommandOption<Double> RATE = basic("rate", "The interest rate expressed as a percent. (Enter 5.2 for 5.2%)", OptionType.NUMBER,
        OptionMapping::getAsDouble);
    CommandOption<String> DISCOUNT = basic("discount", "Vouchers & Referral Codes", OptionType.NUMBER,
        OptionMapping::getAsString);


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
        return getRequired(event, ErrorMessages.missingOption(getOptionName()));
    }

    default R getRequired(SlashCommandInteractionEvent event, AmbrosiaMessage errorMsg) {
        R result = getOptional(event);
        if (result == null) errorMsg.replyError(event);
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
