package com.ambrosia.loans.discord.command.staff.modify;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.request.base.ModifyRequestMsg;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoanGui;
import com.ambrosia.loans.discord.request.loan.BaseModifyLoanRequest;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public class AModifyLoanCommand extends BaseSubCommand implements BaseModifyLoanRequest {


    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ActiveRequestLoanGui loan = findLoanRequest(event, true);
        if (loan == null) return;
        List<ModifyRequestMsg> changes = new ArrayList<>();
        changes.add(setVouch(loan, event));
        changes.add(setRate(loan, event));
        changes.add(setInitialAmount(loan, event));
        changes.add(setDate(loan, event));
        replyChanges(event, changes, loan);
    }

    private ModifyRequestMsg setDate(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        Instant startDate = CommandOption.LOAN_START_DATE.getOptional(event);
        if (startDate == null) return null;
        try {
            if (startDate.isAfter(Instant.now()))
                return ModifyRequestMsg.error("Cannot set the start date in the future");

            loan.getData().setStartDate(startDate);
            return ModifyRequestMsg.info("Set the start date to %s".formatted(formatDate(startDate)));
        } catch (DateTimeParseException e) {
            String startDateString = CommandOption.LOAN_START_DATE.getMap1(event);
            String msg = ErrorMessages.dateParseError(startDateString, "MM/DD/YY").toString();
            return ModifyRequestMsg.error(msg);
        }
    }

    private ModifyRequestMsg setRate(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        @Nullable Double rate = CommandOption.RATE.getOptional(event);
        if (rate == null) return null;
        if (rate < 0)
            return ModifyRequestMsg.error("Rate must be positive!");
        loan.getData().setRate(rate / 100);
        schedule(() -> loan.updateSender("**{endorser}** has set the interest rate as %s.".formatted(formatPercentage(rate / 100))));
        if (rate < 1) {
            String msg = "Set rate as %s. Are you sure you want to set it less than 1%%?".formatted(formatPercentage(rate / 100));
            return ModifyRequestMsg.warning(msg);
        }
        return ModifyRequestMsg.info("Set rate as %s.".formatted(formatPercentage(rate / 100)));
    }

    private ModifyRequestMsg setInitialAmount(ActiveRequestLoanGui loan, SlashCommandInteractionEvent event) {
        @Nullable Emeralds amount = CommandOption.LOAN_INITIAL_AMOUNT.getOptional(event);
        if (amount == null) return null;
        if (amount.lte(0))
            return ModifyRequestMsg.error("Rate must be positive!");
        loan.getData().setInitialAmount(amount);
        schedule(() -> {
            loan.updateSender("**{endorser}** has set the initial amount to %s.".formatted(amount));
        });
        if (amount.gte(Emeralds.stxToEmeralds(64).amount())) {
            String msg = "Set amount as %s. Are you sure you want to set it over 64 stx?".formatted(amount);
            return ModifyRequestMsg.warning(msg);
        }
        return ModifyRequestMsg.info("Set amount as %s.".formatted(amount));
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
            List.of(CommandOption.LOAN_INITIAL_AMOUNT, CommandOption.RATE, CommandOption.LOAN_VOUCH, CommandOption.LOAN_START_DATE)
        ).addToCommand(command);

        return command;
    }
}
