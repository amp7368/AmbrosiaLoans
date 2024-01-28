package com.ambrosia.loans.discord.commands.manager.modify;

import static com.ambrosia.loans.discord.DiscordModule.SIMPLE_DATE_FORMATTER;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.modify.BaseModifyLoanRequest;
import com.ambrosia.loans.discord.base.command.modify.ModifyRequestMsg;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class StaffModifyLoanCommand extends BaseSubCommand implements BaseModifyLoanRequest {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui loan = findLoanRequest(event, true);
        if (loan == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setVouch(loan, event));
        changes.add(setRate(loan, event));
        changes.add(setDate(loan, event));
        replyChanges(event, changes, loan);
    }

    private ModifyRequestMsg setDate(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        String startDateString = findOption(event, "start_date", OptionMapping::getAsString);
        if (startDateString == null) return null;
        try {
            TemporalAccessor parsed = SIMPLE_DATE_FORMATTER.parse(startDateString);
            Instant startDate = Instant.from(parsed);
            if (startDate.isAfter(Instant.now()))
                return ModifyRequestMsg.error("Cannot set the start date in the future");
            loan.getData().setStartDate(startDate);
            return ModifyRequestMsg.info("Set the start date to %s".formatted(SIMPLE_DATE_FORMATTER.format(parsed)));
        } catch (DateTimeParseException e) {
            return ModifyRequestMsg.error("Failed to parse date. Please use format MM/dd/yy");
        }
    }

    private ModifyRequestMsg setRate(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        @Nullable Double rate = CommandOption.RATE.getOptional(event);
        if (rate == null) return null;
        if (rate < 0)
            return ModifyRequestMsg.error("Rate must be positive!");
        loan.getData().setRate(rate / 100);
        if (rate < 1) {
            String msg = "Set rate as %s. Are you sure you want to set it less than 1%%?".formatted(formatPercentage(rate / 100));
            return ModifyRequestMsg.warning(msg);
        }
        return ModifyRequestMsg.info("Set rate as %s.".formatted(formatPercentage(rate / 100)));
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("loan", "[Staff] Modify a loan request");
        CommandOptionList.of(
            List.of(CommandOption.REQUEST),
            List.of(CommandOption.RATE, CommandOption.VOUCH)
        ).addToCommand(command);
        command.addOption(OptionType.STRING, "start_date",
            "The start date (MM/DD/YY) for the loan. (Defaults to approval date if not specified)");

        return command;
    }
}
