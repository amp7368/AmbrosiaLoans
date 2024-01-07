package com.ambrosia.loans.discord.base.command;

import com.ambrosia.loans.discord.base.AmbrosiaColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SendMessage {

    default EmbedBuilder success() {
        return new EmbedBuilder().setColor(AmbrosiaColor.SUCCESS);
    }

    default MessageEmbed success(String msg) {
        return success().setDescription(msg).build();
    }

    default EmbedBuilder error() {
        return new EmbedBuilder().setColor(AmbrosiaColor.BAD);
    }

    default MessageEmbed error(String msg) {
        return error().setDescription(msg).build();
    }

    default MessageEmbed badRole(String role, SlashCommandInteractionEvent event) {
        return error(String.format("You must be a %s to run '/%s'", role, event.getFullCommandName()));
    }

    default MessageEmbed missingOption(String option) {
        return error(String.format("'%s' is required", option));
    }

    default void missingOption(SlashCommandInteractionEvent event, String option) {
        event.replyEmbeds(missingOption(option)).queue();
    }

    default void errorRegisterWithStaff(SlashCommandInteractionEvent event) {
        event.replyEmbeds(error("To register your account use **/request account** and fill in your Minecraft username."))
            .setEphemeral(true).queue();
    }

    default MessageEmbed errorRegisterWithStaff() {
        return error("To register your account use **/request account** and fill in your Minecraft username.");
    }
}
