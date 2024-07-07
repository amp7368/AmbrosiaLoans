package com.ambrosia.loans.discord.base.command.option;

import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.investment.InvestApi.InvestQueryApi;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.account.withdrawal.WithdrawalApi.WithdrawalQueryApi;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandOption<R> {

    // help
    CommandOptionBasic<Integer> HELP_PAGE = basic("page", "The page to jump to", OptionType.STRING, OptionMapping::getAsInt)
        .addChoices(List.of(
            new Choice("Home", 0),
            new Choice("Loan Information", 0),
            new Choice("Loan Commands", 0),
            new Choice("Investor Information", 0),
            new Choice("Investor Commands", 0),
            new Choice("Extra", 0)
        ));

    // client
    CommandOptionMulti<String, DClient> CLIENT = multi("client", "Client associated with this action", OptionType.STRING,
        OptionMapping::getAsString, ClientQueryApi::findByName).setAutocomplete();
    CommandOption<Member> DISCORD = basic("discord", "The discord of the client", OptionType.MENTIONABLE,
        OptionMapping::getAsMember);
    CommandOption<String> MINECRAFT = basic("minecraft", "Your minecraft username", OptionType.STRING,
        OptionMapping::getAsString);
    CommandOption<String> DISPLAY_NAME = basic("display_name", "The name to display on the profile", OptionType.STRING,
        OptionMapping::getAsString);

    // common
    CommandOptionDate DATE = new CommandOptionDate();
    CommandOptionDate LOAN_START_DATE = new CommandOptionDate("start_date",
        "The start date (MM/DD/YY) for the loan. (Defaults to current date if not specified)");

    // request
    CommandOptionMulti<Long, DCFStoredGui<?>> REQUEST = multi("request_id", "The id of the request", OptionType.INTEGER,
        OptionMapping::getAsLong, ActiveRequestDatabase.get()::getRequest);
    CommandOptionMulti<Double, Emeralds> PAYMENT_AMOUNT = emeraldsAmount("pay back");
    CommandOptionMulti<Double, Emeralds> INVESTMENT_AMOUNT = emeraldsAmount("invest");
    CommandOptionMulti<Double, Emeralds> WITHDRAWAL_AMOUNT = emeraldsAmount("withdrawal");
    CommandOption<Boolean> PAYMENT_FULL = full("paying");
    CommandOption<Boolean> WITHDRAWAL_FULL = full("withdrawing");
    // loan request
    CommandOptionMulti<Double, Emeralds> LOAN_INITIAL_AMOUNT = emeraldsAmount("initial_amount");
    CommandOption<Attachment> LOAN_COLLATERAL = basic("image", "Image of the collateral to add to the request",
        OptionType.ATTACHMENT, OptionMapping::getAsAttachment);
    CommandOption<String> LOAN_COLLATERAL_DESCRIPTION = basic("description", "Description for the collateral",
        OptionType.STRING, OptionMapping::getAsString);
    CommandOption<Long> LOAN_COLLATERAL_REQUEST_ID = basic("collateral_id", "The id of the collateral for this request",
        OptionType.INTEGER, OptionMapping::getAsLong);
    CommandOption<String> LOAN_COLLATERAL_NAME = basic("name", "Name for the collateral",
        OptionType.STRING, OptionMapping::getAsString);

    // loan
    CommandOptionMulti<String, DClient> LOAN_VOUCH = multi("vouch", "Referral/vouch from someone with credit with Ambrosia",
        OptionType.STRING, OptionMapping::getAsString, ClientQueryApi::findByName).setAutocomplete();
    CommandOption<Double> RATE = basic("rate", "The interest rate expressed as a percent. (Enter 5.2 for 5.2%)", OptionType.NUMBER,
        OptionMapping::getAsDouble);
    CommandOption<String> LOAN_DISCOUNT = basic("discount", "Vouchers & Referral Codes", OptionType.NUMBER,
        OptionMapping::getAsString);
    CommandOptionMulti<Double, Duration> LOAN_FREEZE_DURATION = multi("duration_days", "The number of days to freeze the loan for",
        OptionType.NUMBER, OptionMapping::getAsDouble, d -> Duration.ofMillis((long) (86400L * d) * 1000));

    // staff query
    CommandOptionMulti<Long, DLoan> LOAN_ID = multi("loan_id", "The id of the loan", OptionType.INTEGER,
        OptionMapping::getAsLong, LoanQueryApi::findById);
    CommandOptionMulti<Long, DLoanPayment> PAYMENT_ID = multi("payment_id", "The id of the payment", OptionType.INTEGER,
        OptionMapping::getAsLong, LoanQueryApi::findPaymentById);
    CommandOptionMulti<Long, DWithdrawal> WITHDRAWAL_ID = multi("withdrawal_id", "The id of the withdrawal", OptionType.INTEGER,
        OptionMapping::getAsLong, WithdrawalQueryApi::findById);
    CommandOptionMulti<Long, DInvestment> INVESTMENT_ID = multi("investment_id", "The id of the investment", OptionType.INTEGER,
        OptionMapping::getAsLong, InvestQueryApi::findById);


    // undo/redo
    CommandOptionMulti<Long, DAlterChange> MODIFICATION_ID = multi("modification_id", "The modification target",
        OptionType.INTEGER, OptionMapping::getAsLong, AlterQueryApi::findChangeById);
    CommandOption<Long> DELETE_ENTITY_ID = basic("entity_id", "The id of the entity to delete", OptionType.STRING,
        OptionMapping::getAsLong);
    CommandOption<AlterCreateType> DELETE_ENTITY_TYPE = deleteEntity();

    // comments
    CommandOption<String> COMMENT = basic("comment", "The comment you want to make", OptionType.STRING,
        OptionMapping::getAsString);

    // config
    CommandOption<String> CONFIG_TOS_LINK = basic("link", "The link for this version of the TOS", OptionType.STRING,
        OptionMapping::getAsString);
    CommandOption<String> CONFIG_TOS_VERSION = basic("version", "The version (ie: 'v1.2') for this version of the TOS",
        OptionType.STRING, OptionMapping::getAsString);

    static CommandOptionBasic<AlterCreateType> deleteEntity() {
        CommandOptionMulti<String, AlterCreateType> option = CommandOption.multi("delete_entity", "The entity type to delete",
            OptionType.STRING, OptionMapping::getAsString, name -> AlterCreateType.valueOf(name.toUpperCase()));
        List<Choice> choices = Arrays.stream(AlterCreateType.values())
            .map(s -> new Choice(s.displayName(), s.name().toLowerCase()))
            .toList();
        return option.addChoices(choices);
    }

    @NotNull
    static CommandOption<Boolean> full(String verb) {
        String desc = "True if %s the full amount".formatted(verb);
        return basic("full", desc, OptionType.BOOLEAN, OptionMapping::getAsBoolean);

    }

    @NotNull
    static CommandOptionMulti<Double, Emeralds> emeraldsAmount(String type) {
        String desc = "The amount to %s. Expressed in STX. (Enter 0.5 for 32LE)".formatted(type);
        return multi("amount", desc, OptionType.NUMBER,
            OptionMapping::getAsDouble, Emeralds::stxToEmeralds);
    }


    static <V, R> CommandOptionMulti<V, R> multi(String name, String description, OptionType type,
        Function<OptionMapping, V> mapping1, Function<V, R> mapping2) {
        return new CommandOptionMulti<>(name, description, type, mapping1, mapping2);
    }

    private static <R> CommandOptionBasic<R> basic(String name, String description, OptionType type,
        Function<OptionMapping, R> getOption) {
        return new CommandOptionBasic<>(name, description, type, getOption);
    }

    R getOptional(CommandInteraction event, R fallback);

    @Nullable
    default R getOptional(CommandInteraction event) {
        return this.getOptional(event, null);
    }

    default AmbrosiaMessage getErrorMessage(CommandInteraction event) {
        return ErrorMessages.missingOption(getOptionName());
    }

    default R getRequired(CommandInteraction event) {
        return getRequired(event, getErrorMessage(event));
    }

    default R getRequired(CommandInteraction event, AmbrosiaMessage errorMsg) {
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
