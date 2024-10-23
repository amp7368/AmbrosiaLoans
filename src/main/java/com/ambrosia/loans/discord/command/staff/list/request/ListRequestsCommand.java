package com.ambrosia.loans.discord.command.staff.list.request;

import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import discord.util.dcf.gui.base.gui.DCFGui;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListRequestsCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DCFGui gui = new DCFGui(dcf, event::reply);
        gui.addPage(new ListRequestsPage(gui));
        gui.send();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("requests", "List active requests");
    }
}
