package com.ambrosia.loans.discord.command.staff.find.loan;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class FindLoanCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DLoan loan = CommandOption.LOAN_ID.getRequired(event, ErrorMessages.noEntityWithId("loan"));
        if (loan == null) return;

        DCFGui gui = new DCFGui(dcf, event::reply);
        new FindLoanMessage(gui, loan)
            .addPageToGui()
            .send();
    }

    @Override
    public SubcommandData getData() {
        return CommandOptionList.of(
            List.of(CommandOption.LOAN_ID)
        ).addToCommand(new SubcommandData("loan", "[Staff] Find loan by id"));
    }
}
