package com.ambrosia.loans.discord.commands.manager.modify;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.modify.BaseModifyLoanRequest;
import com.ambrosia.loans.discord.base.command.modify.ModifyRequestMsg;
import com.ambrosia.loans.discord.request.cash.ActiveRequestLoanGui;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class CommandStaffModifyLoan extends BaseSubCommand implements BaseModifyLoanRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui loan = findLoanRequest(event);
        if (loan == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setVouch(loan, event));
        changes.add(setRate(loan, event));
        changes.add(setDate(loan, event));
        replyChanges(event, changes, loan);
    }

    private ModifyRequestMsg setDate(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String startDate = findOption(event, "start_date", OptionMapping::getAsString);
        if (startDate == null) return null;
        try {
            DateTimeFormatter format = new DateTimeFormatterBuilder()
                .appendPattern("MM/dd/yy")
                .parseDefaulting(ChronoField.SECOND_OF_DAY, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter()
                .withZone(DiscordModule.TIME_ZONE);
            TemporalAccessor parsed = format.parse(startDate);
            loan.getData().setStartDate(Instant.from(parsed));
            return ModifyRequestMsg.info("Set the start date to %s".formatted(format.format(parsed)));
        } catch (DateTimeParseException e) {
            return ModifyRequestMsg.error("Failed to parse date. Please use format MM/dd/yy");
        }
    }

    private ModifyRequestMsg setRate(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        @Nullable Double rate = findOption(event, "rate", OptionMapping::getAsDouble);
        if (rate == null) return null;
        if (rate < 0)
            return ModifyRequestMsg.error("Rate must be positive!");
        loan.getData().setRate(rate / 100);
        if (rate < 1) {
            String msg = "Set rate as %s. Are you sure you want to set it less than 1%%?".formatted(formatPercentage(rate / 100));
            return ModifyRequestMsg.warning(msg);
        }
        return ModifyRequestMsg.info("Set rate as %f%%.".formatted(rate));
    }


    @Override
    public SubcommandData getData() {
        return new SubcommandData("loan", "Modify loan request")
            .addOptions(optionRequestId())
            .addOption(OptionType.STRING, "start_date",
                "The start date (MM/DD/YY) for the loan. (Defaults to approval date if not specified)")
            .addOption(OptionType.NUMBER, "rate", "The interest rate expressed as a percent. (Enter 5.2 for 5.2%)")
            .addOptions(optionVouch());
    }
}
