package com.ambrosia.loans.discord.command.staff.alter.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.alter.db.DAlterChangeRecord;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckDate;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanDefaultCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCommandField<DLoan> loan = field(CommandOption.LOAN_ID);
        AlterCommandField<Instant> date = field(CommandOption.DATE, new CheckDate());

        CheckErrorList errors = getAndCheckErrors(event, List.of(loan), List.of(date));
        if (errors.hasError()) return;
        Instant dateValue = date.getOrDefault(Instant.now());
        DAlterChangeRecord change = LoanAlterApi.setDefaulted(staff, loan.get(), dateValue);
        String successMsg = "Set loan as defaulted on %s".formatted(formatDate(dateValue));

        ReplyAlterMessage message = ReplyAlterMessage.of(change, successMsg);

        replyChange(event, errors, message);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("default", "Default a loan");
        CommandOptionList.of(
            List.of(CommandOption.LOAN_ID),
            List.of(CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
