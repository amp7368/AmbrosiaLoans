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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanFreezeCommand extends BaseAlterCommand {

    private static final Duration WARN_FREEZE_MIN = Duration.ofDays(7);
    private static final Duration WARN_FREEZE_MAX = Duration.ofDays(365);

    private CheckErrorList checkErrors(DLoan loan, Duration duration) {
        CheckErrorList errors = CheckErrorList.of();
        if (loan.isPaid())
            errors.addError("Loan is already paid!");
        else if (loan.isFrozen()) {
            errors.addError("Loan is already frozen! Unfreeze the loan before trying to freeze the loan again! "
                + "(or undo the last freeze modification)");
        }

        if (!duration.isPositive()) {
            errors.addError("Duration must be positive!");
        } else if (duration.compareTo(WARN_FREEZE_MIN) < 0) {
            String clientName = loan.getClient().getEffectiveName();
            long days = duration.toDays();
            String msg = "Set to unfreeze %s's loan in %s days. Are you sure you want to unfreeze in less than 7 days?"
                .formatted(clientName, days);
            errors.addWarning(msg);
        } else if (duration.compareTo(WARN_FREEZE_MAX) > 0) {
            String clientName = loan.getClient().getEffectiveName();
            long days = duration.toDays();
            String msg = "Set to unfreeze %s's loan in %s days. Are you sure you want to unfreeze in more than 365 days?"
                .formatted(clientName, days);
            errors.addWarning(msg);
        }

        return errors;
    }

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DLoan loan = CommandOption.LOAN_ID.getRequired(event);
        if (loan == null) return;
        Duration duration = CommandOption.LOAN_FREEZE_DURATION.getRequired(event);
        if (duration == null) return;

        CheckErrorList errors = checkErrors(loan, duration);
        if (errors.hasError()) {
            errors.reply(event);
            return;
        }

        Instant effectiveDate = Instant.now();
        Instant unfreezeDate = effectiveDate.plus(duration);
        Double unfreezeRate = loan.getRateAt(effectiveDate);
        if (unfreezeRate == null) {
            String msg = "Somehow there is no interest rate set on the loan at %s? Inform staff of this error."
                .formatted(formatDate(effectiveDate));
            replyError(event, msg);
            return;
        }

        DAlterChange change = LoanAlterApi.freeze(staff, loan, effectiveDate, unfreezeRate, unfreezeDate);
        String clientName = loan.getClient().getEffectiveName();
        String successMsg = "Set %s's loan %s as frozen on %s. The loan will return to an interest rate of %s on %s."
            .formatted(clientName, loan.getId(), formatDate(effectiveDate), formatPercentage(unfreezeRate),
                formatDate(unfreezeDate));

        ReplyAlterMessage message = ReplyAlterMessage.of(change, successMsg);

        replyChange(event, errors, message);
    }


    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("freeze", "Freeze a loan for a certain duration");
        CommandOptionList.of(
            List.of(CommandOption.LOAN_ID, CommandOption.LOAN_FREEZE_DURATION)
        ).addToCommand(command);
        return command;
    }
}
