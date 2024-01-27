package com.ambrosia.loans.discord.system.theme;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.request.ActiveRequestStage;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class AmbrosiaMessages {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("LLLL dd yyyy")
        .withZone(DiscordModule.TIME_ZONE);

    public static String formatDate(Instant date) {
        return DATE_FORMATTER.format(date);
    }

    public static String formatPercentage(double perc) {
        return "%.2f%%".formatted(perc * 100);
    }

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

        public static AmbrosiaMessage noRequestWithId(Long requestId) {
            String msg = "There is no request with id '%d'!".formatted(requestId);
            return error(msg);
        }

        public static AmbrosiaMessage badRequestType(String type, Long requestId) {
            String msg = "Request #%d is not a %s request".formatted(requestId, type);
            return error(msg);
        }

        public static AmbrosiaMessage amountNotPositive(Emeralds amount) {
            String msg = "Provided amount: %s is not positive!".formatted(amount);
            return error(msg);
        }

        public static AmbrosiaMessage paymentTooMuch(Emeralds balance, Emeralds payment) {
            String msg = "Cannot pay back %s. You only owe %s!".formatted(payment, balance);
            return error(msg);
        }

        public static AmbrosiaMessage onlyLoans(Emeralds balance) {
            String msg = "You do not have any loans since your balance is %s!".formatted(balance);
            return error(msg);
        }

        public static AmbrosiaMessage notClient(DClient client) {
            String msg = "This is %s's!".formatted(client.getEffectiveName());
            return error(msg);
        }

        public static AmbrosiaMessage hasActiveLoan(DLoan loan) {
            String msg = "You have an active loan of %s!".formatted(loan.getTotalOwed());
            return error(msg);
        }

        public static AmbrosiaMessage onlyInvestments(Emeralds balance) {
            String msg = "You cannot make an investment since you owe %s!".formatted(balance.negative());
            return error(msg);
        }

        public static AmbrosiaMessage withdrawalTooMuch(Emeralds withdrawal, Emeralds balance) {
            String msg = "Cannot withdrawal back %s. You only have %s!".formatted(withdrawal, balance);
            return error(msg);
        }

        public static AmbrosiaMessage cannotModifyRequestAtStage(ActiveRequestStage stage) {
            String msg = "Client's cannot modify a request that is in the '%s' stage".formatted(stage);
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
