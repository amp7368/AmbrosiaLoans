package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.discord.system.theme.DiscordEmojis;
import java.util.function.Function;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

public interface SendMessage {

    static SendMessage get() {
        return new SendMessage() {
        };
    }

    default EmbedBuilder success() {
        return new EmbedBuilder().setColor(AmbrosiaColor.SUCCESS);
    }

    default MessageEmbed success(String msg) {
        return success().setDescription(msg).build();
    }

    default EmbedBuilder error() {
        return new EmbedBuilder()
            .setAuthor("Error! " + DiscordEmojis.DENY)
            .setColor(AmbrosiaColor.BAD);
    }

    default MessageEmbed error(String msg) {
        return error().setDescription(msg).build();
    }

    @Nullable
    default <T> T findOption(SlashCommandInteractionEvent event, String optionName, Function<OptionMapping, T> getAs) {
        return findOption(event, optionName, getAs, false);
    }

    @Nullable
    default <T> T findOption(SlashCommandInteractionEvent event, String optionName, Function<OptionMapping, T> getAs,
        boolean isRequired) {
        OptionMapping option = event.getOption(optionName);
        if (option == null || getAs.apply(option) == null) {
            if (isRequired)
                ErrorMessages.missingOption(optionName)
                    .replyError(event);
            return null;
        }
        return getAs.apply(option);
    }

    default void replyError(SlashCommandInteractionEvent event, String msg) {
        event.replyEmbeds(error(msg))
            .setEphemeral(true)
            .queue();
    }

    default void replyError(SlashCommandInteractionEvent event, MessageCreateData msg) {
        event.reply(msg)
            .setEphemeral(true)
            .queue();
    }

    default void replySuccess(SlashCommandInteractionEvent event, String msg) {
        event.replyEmbeds(success(msg)).queue();
    }

}
