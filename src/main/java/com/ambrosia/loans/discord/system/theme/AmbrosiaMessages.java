package com.ambrosia.loans.discord.system.theme;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.command.SendMessage;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.request.ActiveRequestStage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.helpers.CheckReturnValue;

public class AmbrosiaMessages {

    public static final String NULL_MSG = "N/A";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("LLLL dd yyyy")
        .withZone(DiscordModule.TIME_ZONE);

    public static String formatDate(Instant date) {
        return formatDate(date, false);
    }

    public static String formatDate(Instant date, boolean emoji) {
        String dateFormatted = DATE_FORMATTER.format(date);
        if (emoji) return AmbrosiaEmoji.ANY_DATE + " " + dateFormatted;
        return dateFormatted;
    }

    public static String formatPercentage(double perc) {
        return "%.2f%%".formatted(perc * 100);
    }

    public static AmbrosiaMessage stringMessage(String msg) {
        return new AmbrosiaStringMessage(msg);
    }

    public static class ErrorMessages {

        @CheckReturnValue
        private static AmbrosiaMessage error(String msg) {
            return new AmbrosiaStringMessage(msg);
        }

        @CheckReturnValue
        private static AmbrosiaCreateMessage error(MessageCreateData msg) {
            return new AmbrosiaCreateMessage(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage invalidOption(String optionName, Object optionValue) {
            return error("Invalid %s. '%s' was provided".formatted(optionName, optionValue));
        }

        @CheckReturnValue
        public static AmbrosiaMessage badRole(String requiredRole, CommandInteraction event) {
            String commandName = event.getFullCommandName();
            return error(String.format("You must be a %s to run '/%s'", requiredRole, commandName));
        }


        @CheckReturnValue
        public static AmbrosiaMessage registerWithStaff() {
            return error("To register your account use **/request account** and fill in your Minecraft "
                + "username.");
        }

        @CheckReturnValue
        public static AmbrosiaMessage missingOption(String option) {
            String msg = String.format("'%s' is required, but was not provided", option);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage onlyInAmbrosia() {
            MessageEmbed embedMsg = SendMessage.get().error("Can only be used in Ambrosia's Discord");
            MessageCreateData msg = new MessageCreateBuilder()
                .setEmbeds(embedMsg)
                .setActionRow(DiscordModule.inviteButton())
                .build();
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage rejectedTOSRequest(String type) {
            String desc =
                ("Since TOS was rejected, your %s request cannot be accepted"
                    + " and was deleted. Staff have not been notified of your request.")
                    .formatted(type);
            MessageEmbed embed = new EmbedBuilder()
                .setColor(AmbrosiaColor.RED)
                .setTitle("Rejected TOS")
                .setDescription(desc)
                .build();
            return error(MessageCreateData.fromEmbeds(embed));
        }


        @CheckReturnValue
        public static AmbrosiaMessage cannotDeleteProfile(DClient client) {
            String msg = String.format("Cannot delete %s's profile. There are entries associated with their account",
                client.getDisplayName());
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage noRequestWithId(Long requestId) {
            String msg = "There is no request with id '%d'!".formatted(requestId);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage noCollateralWithId(long requestId, long id) {
            String msg = "There is no collateral in request %s %d with id '%d'!"
                .formatted(AmbrosiaEmoji.KEY_ID_CHANGES, requestId, id);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage badRequestType(String type, Long requestId) {
            String msg = "Request #%d is not a %s request".formatted(requestId, type);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage amountNotPositive(Emeralds amount) {
            String msg = "Provided amount: %s is not positive!".formatted(amount);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage paymentTooMuch(Emeralds balance, Emeralds payment) {
            String msg = "Cannot pay back %s. You only owe %s!".formatted(payment, balance);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage onlyLoans() {
            return error("You do not have any active loans!");
        }

        @CheckReturnValue
        public static AmbrosiaMessage notCorrectClient(DClient client) {
            String msg = "This is %s's!".formatted(client.getEffectiveName());
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage hasActiveLoan(DLoan loan) {
            String msg = "You have an active loan of %s!".formatted(loan.getTotalOwed());
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage onlyInvestments(Emeralds balance) {
            String msg = "You cannot make an investment since you owe %s!".formatted(balance.negative());
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage withdrawalTooMuch(Emeralds withdrawal, Emeralds balance) {
            String msg = "Cannot withdrawal back %s. You only have %s!".formatted(withdrawal, balance);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage cannotModifyRequestAtStage(ActiveRequestStage stage) {
            String msg = "Clients cannot modify a request that is in the '%s' stage".formatted(stage);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage blacklisted() {
            String msg = "You're blacklisted and can no longer interact with the bot";
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage alteredAlready(String action) {
            String msg = "Action is already %s".formatted(action);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage dateParseError(String given, String expected) {
            String msg = "Failed to parse date. %s is not in the format %s".formatted(given, expected);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage uploadSizeTooLarge(int maxMB, double actualUploadMB) {
            String msg = "Upload size too large! %.2fMB is greater than the %dMB limit!".formatted(actualUploadMB, maxMB);
            return error(msg);
        }

        @CheckReturnValue
        public static AmbrosiaMessage onlyUploadImages() {
            String msg = "Images are the only filetype allowed!";
            return error(msg);
        }

        public static AmbrosiaMessage textTooLong(int length, int maxLength, CommandOption<?> option) {
            return textTooLong(length, maxLength, option.getOptionName());
        }

        public static AmbrosiaMessage textTooLong(int length, int maxLength, String arg) {
            String msg = "%s is too long! %d characters is greater than the %d limit".formatted(arg, length, maxLength);
            return error(msg);
        }

        public static AmbrosiaMessage emeraldsFormat() {
            String msg = "Use the format \"23 STX 12 LE 8 EB 56 E\" or \"12.75 STX\".";
            return error(msg);
        }
    }

    private record AmbrosiaStringMessage(String msg) implements AmbrosiaMessage, SendMessage {

        @Override
        public void replyError(CommandInteraction event) {
            replyError(event, msg);
        }

        @Override
        public MessageCreateData createMsg() {
            return MessageCreateData.fromContent(msg);
        }

        @Override
        public String toString() {
            return this.msg;
        }
    }

    private record AmbrosiaCreateMessage(MessageCreateData msg) implements AmbrosiaMessage, SendMessage {

        @Override
        public void replyError(CommandInteraction event) {
            replyError(event, msg);
            msg.close();
        }

        @Override
        public MessageCreateData createMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return this.msg.getContent();
        }
    }
}
