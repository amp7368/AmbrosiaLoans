package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseStaffSubCommand extends BaseSubCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        DStaffConductor staff = StaffCommandUtil.getOrMakeStaff(event);
        if (staff == null) return;
        this.onStaffCommand(event, staff);
    }

    protected abstract void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff);
}
