package com.ambrosia.loans.discord.command.staff.alter.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.loan.CheckInitialAmount;
import com.ambrosia.loans.discord.check.loan.CheckStart;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanAlterCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCommandField<DLoan> loan = field(CommandOption.LOAN_ID);
        AlterCommandField<Instant> startDate = field(CommandOption.LOAN_START_DATE, new CheckStart());
        AlterCommandField<Emeralds> initialAmount = field(CommandOption.LOAN_INITIAL_AMOUNT, new CheckInitialAmount());

        CheckErrorList errors = getAndCheckErrors(event,
            List.of(loan),
            List.of(initialAmount, startDate));
        if (errors.hasError()) return;

        ReplyAlterMessage message = new ReplyAlterMessage();
        if (startDate.exists()) {
            DAlterChange change = LoanAlterApi.setStartDate(staff, loan.get(), startDate.get());
            String successMsg = "Set start date to %s".formatted(AmbrosiaMessages.formatDate(startDate.get()));
            message.add(change, successMsg);
        }
        if (initialAmount.exists()) {
            DAlterChange change = LoanAlterApi.setInitialAmount(staff, loan.get(), initialAmount.get());
            String successMsg = "Set initial amount to %s".formatted(initialAmount);
            message.add(change, successMsg);
        }
        replyChange(event, errors, message);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("alter", "Alter something about a loan.");
        CommandOptionList.of(
            List.of(CommandOption.LOAN_ID),
            List.of(CommandOption.LOAN_INITIAL_AMOUNT, CommandOption.LOAN_START_DATE)
        ).addToCommand(command);
        return command;
    }
}
