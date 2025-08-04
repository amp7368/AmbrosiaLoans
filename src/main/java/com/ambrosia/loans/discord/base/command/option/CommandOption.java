package com.ambrosia.loans.discord.base.command.option;

import apple.utilities.util.Pretty;
import com.ambrosia.loans.database.account.collateral.CollateralApi;
import com.ambrosia.loans.database.account.collateral.DCollateral;
import com.ambrosia.loans.database.account.collateral.DCollateralStatus;
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
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.entity.staff.StaffConductorApi;
import com.ambrosia.loans.discord.request.ActiveRequestDatabase;
import com.ambrosia.loans.discord.system.help.HelpCommandListType;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.clover.CloverRequest.CloverTimeResolution;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.stored.DCFStoredGui;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
    CommandOptionDate FILTER_START_DATE = new CommandOptionDate("filter_start_date",
        "Filter to anything after this start date (MM/DD/YY).");
    CommandOptionDate FILTER_END_DATE = new CommandOptionDate("filter_end_date",
        "Filter to anything before this end date (MM/DD/YY).");

    // request
    CommandOptionMulti<Long, DCFStoredGui<?>> REQUEST = multi("request_id", "The id of the request", OptionType.INTEGER,
        OptionMapping::getAsLong, ActiveRequestDatabase.get()::getRequest);
    CommandOptionMulti<String, Emeralds> PAYMENT_AMOUNT = emeraldsAmount(null, "pay back");
    CommandOptionMulti<String, Emeralds> COLLATERAL_SOLD_AMOUNT = emeraldsAmount("sold_for", "sell the collateral for");
    CommandOptionMulti<String, Emeralds> INVESTMENT_AMOUNT = emeraldsAmount(null, "invest");
    CommandOptionMulti<String, Emeralds> WITHDRAWAL_AMOUNT = emeraldsAmount(null, "withdrawal");
    CommandOptionMulti<String, Emeralds> LOAN_INTEREST_CAP = emeraldsAmount("interest_cap", "cap interest to");
    CommandOption<Boolean> PAYMENT_FULL = full("paying");
    CommandOption<Boolean> WITHDRAWAL_FULL = full("withdrawing");
    // loan request
    CommandOptionMulti<String, Emeralds> LOAN_INITIAL_AMOUNT = emeraldsAmount(null, "initial_amount");
    CommandOption<Attachment> LOAN_COLLATERAL_IMAGE = basic("image", "Image of the collateral to add to the request",
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
    CommandOptionMulti<String, DCollateralStatus> LOAN_COLLATERAL_STATUS = new CommandOptionMapEnum<>(
        "status", "The status of the collateral",
        DCollateralStatus.class, DCollateralStatus.commandChoices());
    // staff query
    CommandOptionMulti<Long, DLoan> LOAN_ID = multi("loan_id", "The id of the loan", OptionType.INTEGER,
        OptionMapping::getAsLong, LoanQueryApi::findById);
    CommandOptionMulti<Long, DCollateral> COLLATERAL_ID = multi("collateral_id", "The id of the collateral", OptionType.INTEGER,
        OptionMapping::getAsLong, id -> CollateralApi.findById(id));
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
    CommandOptionMulti<String, HelpCommandListType> HELP_LIST_TYPE = new CommandOptionMapEnum<>("help_list", "The type of help list",
        HelpCommandListType.class, HelpCommandListType.values());
    CommandOptionMulti<String, StopStartAction> STOP_START = new CommandOptionMapEnum<>("action", "Stop or start the action",
        StopStartAction.class, StopStartAction.values());

    // misc
    CommandOption<String> SEND_CLIENT_MESSAGE = basic("message", "The message to send to the client",
        OptionType.STRING, OptionMapping::getAsString);
    CommandOption<String> SEND_CLIENT_TITLE = basic("title", "The title of the embed message to send",
        OptionType.STRING, OptionMapping::getAsString)
        .addChoices("Payment", "Loan", "Investment", "Withdrawal", "General");
    CommandOption<Boolean> FORCE = basic("force", "Caution! Whether to force the action.",
        OptionType.BOOLEAN, OptionMapping::getAsBoolean);
    CommandOption<List<String>> KEYWORDS = multi("keywords",
        "List of comma-separated keywords to search for..Example: \"epoch, Olympc,bOrEAl\"",
        OptionType.STRING, OptionMapping::getAsString,
        s -> Arrays.stream(s.split(","))
            .map(String::trim)
            .filter(Predicate.not(String::isBlank))
            .toList());
    CommandOption<CloverTimeResolution> CLOVER_TIME_RESOLUTION = new CommandOptionMapEnum<>("group_by",
        "Group playtime into DAY/WEEK/MONTH chunks", CloverTimeResolution.class, CloverTimeResolution.values());
    CommandOptionMulti<String, DStaffConductor> STAFF = multi("staff", "Associated staff members", OptionType.STRING,
        OptionMapping::getAsString, StaffConductorApi::findByName).setAutocomplete();

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
    static CommandOptionMulti<String, Emeralds> emeraldsAmount(String name, String type) {
        String desc = "The amount to %s. %s".formatted(type, ErrorMessages.emeraldsFormat());
        name = Objects.requireNonNullElse(name, "amount");
        return new CommandOptionEmeralds(name, desc, OptionType.STRING);
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

    class CommandOptionMapEnum<Enm extends Enum<Enm>> extends CommandOptionMulti<String, Enm> {

        CommandOptionMapEnum(String name, String description, Class<Enm> type, Enm[] values) {
            super(name, description, OptionType.STRING, OptionMapping::getAsString, s -> parseCollateral(type, s));
            addChoices(Stream.of(values)
                .map(Enm::name)
                .map(Pretty::spaceEnumWords)
                .map(c -> new Choice(c, c))
                .toList()
            );
        }

        private static <E extends Enum<E>> E parseCollateral(Class<E> type, String s) {
            try {
                return Enum.valueOf(type, s.replace(' ', '_').toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
