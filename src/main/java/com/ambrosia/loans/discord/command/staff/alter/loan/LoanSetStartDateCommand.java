package com.ambrosia.loans.discord.command.staff.alter.loan;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanSetStartDateCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {

    }

    @Override
    public SubcommandData getData() {
        return null;
    }
}
