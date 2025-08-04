package com.ambrosia.loans.discord.command.staff.alter.loan;

import com.ambrosia.loans.database.account.adjust.AdjustApi;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanInterestCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DLoan loan = CommandOption.LOAN_ID.getRequired(event);
        if (loan == null) return;
        Emeralds interestCap = CommandOption.LOAN_INTEREST_CAP.getRequired(event);
        if (interestCap == null) return;
        boolean force = CommandOption.FORCE.getOptional(event, false);

        if (!force && loan.isPaid()) {
            String msg = "Loan %s %s is already paid off!".formatted(AmbrosiaEmoji.KEY_ID, loan.getId());
            replyError(event, msg);
            return;
        }
        Instant date = Instant.now();
        Emeralds totalOwed = loan.getTotalOwed(date);
        Emeralds adjustmentAmount = loan.getAccumulatedInterest(date).minus(interestCap);
        if (!force && adjustmentAmount.gt(totalOwed.amount())) {
            String msg = "Only %s is owed on loan!".formatted(totalOwed);
            replyError(event, msg);
            return;
        }
        DAdjustLoan adjustment = AdjustApi.createAdjustment(staff, loan, adjustmentAmount, date);
        long adjustmentId = adjustment.getId();
        long loanId = loan.getId();
        String msg = "Created adjustment %s %s for loan %s %s"
            .formatted(AmbrosiaEmoji.KEY_ID, adjustmentId, AmbrosiaEmoji.KEY_ID, loanId);
        replySuccess(event, msg);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("interest", "[Staff] Cap/decrease the interest on a loan");
        return CommandOptionList.of(
            List.of(CommandOption.LOAN_ID, CommandOption.LOAN_INTEREST_CAP),
            List.of(CommandOption.FORCE)
        ).addToCommand(command);
    }
}
