package com.ambrosia.loans.discord.command.staff.alter.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.loan.CheckRate;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanSetRateCommand extends BaseAlterCommand {

    private static CheckErrorList checkErrors(DLoan loan, Instant date, Double rate) {
        CheckErrorList errors = CheckErrorList.of();
        if (!loan.isDateDuring(date)) {
            String msg = "%s is not between the start and end date of the loan".formatted(date);
            errors.addError(msg);
        }
        CheckRate.checkAll(errors, rate);
        return errors;
    }

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DLoan loan = CommandOption.LOAN_ID.getRequired(event);
        if (loan == null) return;
        Double rate = CommandOption.RATE.getRequired(event);
        if (rate == null) return;
        Instant date = CommandOption.DATE.getOptional(event, Instant.now());

        CheckErrorList errors = checkErrors(loan, date, rate);
        if (errors.hasError()) {
            errors.reply(event);
            return;
        }
        rate /= 100;

        LoanAlterApi.setRate(staff, loan, rate, date);

        String successMsg = "Set the rate to %s on %s".formatted(formatPercentage(rate), formatDate(date));
        errors.reply(event, successMsg);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("set_rate", "Set the rate of a loan");
        CommandOptionList.of(
            List.of(CommandOption.LOAN_ID, CommandOption.RATE),
            List.of(CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
