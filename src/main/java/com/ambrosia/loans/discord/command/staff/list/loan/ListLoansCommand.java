package com.ambrosia.loans.discord.command.staff.list.loan;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListLoansCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        ListLoansGui gui = new ListLoansGui(dcf, event::reply);
        new ListLoansPage(gui)
            .addPageToGui()
            .send();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loans", "[Staff] List all loans");
    }
}
