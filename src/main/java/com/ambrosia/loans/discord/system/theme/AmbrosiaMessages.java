package com.ambrosia.loans.discord.system.theme;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.SendMessage;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class AmbrosiaMessages {

    public static class ErrorMessages {

        private static AmbrosiaMessage error(String msg) {
            return new AmbrosiaStringMessage(msg);
        }

        private static AmbrosiaCreateMessage error(MessageCreateData msg) {
            return new AmbrosiaCreateMessage(msg);
        }

        public static AmbrosiaMessage badRole(String requiredRole, SlashCommandInteractionEvent event) {
            String commandName = event.getFullCommandName();
            return error(String.format("You must be a %s to run '/%s'", requiredRole, commandName));
        }


        public static AmbrosiaMessage registerWithStaff() {
            return error("To register your account use **/request account** and fill in your Minecraft "
                + "username.");
        }

        public static AmbrosiaMessage missingOption(String option) {
            String msg = String.format("'%s' is required", option);
            return error(msg);
        }

        public static AmbrosiaMessage onlyInAmbrosia() {
            MessageEmbed embedMsg = SendMessage.get().error("Can only be used in Ambrosia's Discord");
            MessageCreateData msg = new MessageCreateBuilder()
                .setEmbeds(embedMsg)
                .setActionRow(Ambrosia.inviteButton())
                .build();
            return error(msg);
        }


        public static AmbrosiaMessage cannotDeleteProfile(DClient client) {
            String msg = String.format("Cannot delete %s's profile. There are entries associated with their account",
                client.getDisplayName());
            return error(msg);
        }
    }

    private record AmbrosiaStringMessage(String msg) implements AmbrosiaMessage, SendMessage {

        @Override
        public void replyError(SlashCommandInteractionEvent event) {
            replyError(event, msg);
        }

        @Override
        public String toString() {
            return this.msg;
        }
    }

    private record AmbrosiaCreateMessage(MessageCreateData msg) implements AmbrosiaMessage, SendMessage {

        @Override
        public void replyError(SlashCommandInteractionEvent event) {
            replyError(event, msg);
            msg.close();
        }

        @Override
        public String toString() {
            return this.msg.getContent();
        }
    }

}
