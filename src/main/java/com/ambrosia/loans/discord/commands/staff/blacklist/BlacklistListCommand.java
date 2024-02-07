package com.ambrosia.loans.discord.commands.staff.blacklist;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BlacklistListCommand extends BaseSubCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        DCFGui gui = new DCFGui(dcf, event::reply);
        List<DClient> blacklisted = ClientQueryApi.listBlacklisted();
        gui.addPage(new BlacklistMessage(gui, blacklisted));
        gui.send();
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("list", "List everyone on the blacklist");
    }
}
