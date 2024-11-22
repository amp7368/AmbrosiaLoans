package com.ambrosia.loans.discord.base.command.staff;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class BaseStaffCommand extends BaseCommand {

    @Override
    protected final void onCheckedCommand(SlashCommandInteractionEvent event) {
        DStaffConductor staff = StaffCommandUtil.getOrMakeStaff(event);
        if (staff == null) return;
        this.onStaffCommand(event, staff);
    }

    @Override
    public final SlashCommandData getData() {
        SlashCommandData data = getStaffData();
        data.setGuildOnly(true);
        data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MENTION_EVERYONE));
        return data;
    }

    public abstract SlashCommandData getStaffData();

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
    }
}
