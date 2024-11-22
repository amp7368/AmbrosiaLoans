package com.ambrosia.loans.discord.command.staff.alter.collateral;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.database.account.loan.LoanApi.LoanAlterApi;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.collateral.DCollateralStatus;
import com.ambrosia.loans.database.alter.change.DAlterChange;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.check.CheckErrorList;
import com.ambrosia.loans.discord.check.base.CheckDate;
import com.ambrosia.loans.discord.check.base.CheckDateRange;
import com.ambrosia.loans.discord.command.staff.alter.AlterCommandField;
import com.ambrosia.loans.discord.command.staff.alter.BaseAlterCommand;
import com.ambrosia.loans.discord.command.staff.alter.ReplyAlterMessage;
import com.ambrosia.loans.discord.system.theme.AmbrosiaAssets.AmbrosiaEmoji;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ACollateralStatusCommand extends BaseAlterCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        AlterCommandField<DCollateral> collateral = field(CommandOption.COLLATERAL_ID);
        AlterCommandField<DCollateralStatus> status = field(CommandOption.LOAN_COLLATERAL_STATUS);
        AlterCommandField<Instant> date = field(CommandOption.DATE, new CheckDate());

        CheckErrorList errors = getAndCheckErrors(event,
            List.of(collateral, status)
        );
        if (errors.hasError()) return;

        DCollateral dCollateral = collateral.get();
        Instant dateValue = date.getOrDefault(Instant.now());
        new CheckDateRange(dCollateral.getCollectionDate(), null)
            .checkAll(dateValue, errors);
        if (errors.hasError()) {
            errors.reply(event);
            return;
        }

        ReplyAlterMessage message = new ReplyAlterMessage();

        DAlterChange alter = LoanAlterApi.markCollateral(staff, dCollateral, dateValue, status.get());
        String successMsg = "Mark collateral %s %d as %s on %s"
            .formatted(AmbrosiaEmoji.KEY_ID, dCollateral.getId(), status.get(), formatDate(dateValue));
        message.add(alter, successMsg);

        replyChange(event, errors, message);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("status", "[Staff] Set the status of collateral");
        CommandOptionList.of(
            List.of(CommandOption.COLLATERAL_ID, CommandOption.LOAN_COLLATERAL_STATUS),
            List.of(CommandOption.DATE)
        ).addToCommand(command);
        return command;
    }
}
