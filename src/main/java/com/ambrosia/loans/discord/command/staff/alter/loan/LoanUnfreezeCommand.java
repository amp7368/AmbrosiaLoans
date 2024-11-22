package com.ambrosia.loans.discord.command.staff.alter.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;
import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatPercentage;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanUnfreezeCommand extends BaseAlterCommand {

    private CheckErrorList checkErrors(DLoan loan) {
        CheckErrorList errors = CheckErrorList.of();
        if (loan.isPaid())
            errors.addError("Loan is already paid!");
        else if (!loan.isFrozen()) {
            errors.addError("Loan is not frozen!");
        } else if (loan.getMeta().getUnfreezeToRate() == null) {
            errors.addError("Loan does not have unfreezeToRate, even though it's frozen? Inform staff.");
        }
        return errors;
    }

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DLoan loan = CommandOption.LOAN_ID.getRequired(event);
        if (loan == null) return;

        CheckErrorList errors = checkErrors(loan);
        if (errors.hasError()) {
            errors.reply(event);
            return;
        }

        Instant effectiveDate = Instant.now();
        Double previousRate = loan.getRateAt(effectiveDate);
        Double unfreezeToRate = loan.getMeta().getUnfreezeToRate();
        Instant previousUnfreezeDate = loan.getMeta().getUnfreezeDate();
        if (previousRate == null || unfreezeToRate == null || previousUnfreezeDate == null) {
            errors.addError("previousRate or unfreezeToRate is null even though the loan is frozen? Inform staff.");
            return;
        }
        DAlterChange change = LoanAlterApi.unfreeze(staff, loan, effectiveDate, previousRate, unfreezeToRate, previousUnfreezeDate);
        String clientName = loan.getClient().getEffectiveName();
        String successMsg = "Set %s's loan %s as unfrozen on %s. The loan now has an interest rate of %s"
            .formatted(clientName, loan.getId(), formatDate(effectiveDate), formatPercentage(unfreezeToRate));

        ReplyAlterMessage message = ReplyAlterMessage.of(change, successMsg);

        replyChange(event, errors, message);
    }


    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("unfreeze", "[Staff] Unfreeze a frozen loan");
        CommandOptionList.of(
            List.of(CommandOption.LOAN_ID)
        ).addToCommand(command);
        return command;
    }
}
