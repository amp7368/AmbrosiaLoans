package com.ambrosia.loans.discord.command.staff.blacklist;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.command.staff.BaseStaffSubCommand;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BlacklistListCommand extends BaseStaffSubCommand {

    @Override
    protected void onStaffCommand(SlashCommandInteractionEvent event, DStaffConductor staff) {
        DCFGui gui = new DCFGui(dcf, event::reply);
        List<DClient> blacklisted = ClientQueryApi.listBlacklisted();
        gui.addPage(new BlacklistMessage(gui, blacklisted));
        gui.send();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("list", "[Staff] List everyone on the blacklist");
    }
}
