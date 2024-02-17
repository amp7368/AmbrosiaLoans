package com.ambrosia.loans.discord.command.staff.alter.loan;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.amount.CheckLoanAmount;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanSetInitialCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCommandField<DLoan> loan = field(CommandOption.LOAN_ID);
        AlterCommandField<Emeralds> initialAmount = field(CommandOption.LOAN_INITIAL_AMOUNT, new CheckLoanAmount());

        CheckErrorList errors = getAndCheckErrors(event, List.of(loan, initialAmount));
        if (errors.hasError()) return;

        DAlterChangeRecord alter = LoanAlterApi.setInitialAmount(staff, loan.get(), initialAmount.get());

        String successMsg = "Set initial amount to %s".formatted(initialAmount);
        replyChange(event, errors, alter, successMsg);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("initial_amount", "The initial amount of the loan");
        CommandOptionList.of(
            List.of(CommandOption.LOAN_ID, CommandOption.LOAN_INITIAL_AMOUNT)
        ).addToCommand(command);
        return command;
    }
}
