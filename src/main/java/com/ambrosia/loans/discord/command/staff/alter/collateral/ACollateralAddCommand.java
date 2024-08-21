package com.ambrosia.loans.discord.command.staff.alter.collateral;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.command.player.collateral.IAddCollateral;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ACollateralAddCommand extends BaseStaffSubCommand {

    private int id = 100;

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DLoan loan = CommandOption.LOAN_ID.getRequired(event);
        if (loan == null) return;
        IAddCollateral.createCollateral(event, staff, loan, id++);
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("add", "[Staff] Add collateral to a loan");
        return CommandOptionList.of(
            List.of(CommandOption.LOAN_ID),
            List.of(CommandOption.LOAN_COLLATERAL_IMAGE, CommandOption.LOAN_COLLATERAL_NAME, CommandOption.LOAN_COLLATERAL_DESCRIPTION)
        ).addToCommand(command);
    }
}
