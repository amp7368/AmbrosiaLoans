package com.ambrosia.loans.discord.command.staff.list.client;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListClientsCommand extends BaseSubCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ListClientsGui gui = new ListClientsGui(dcf, event::reply);
        gui.addPage(new ListClientsPage(gui));
        gui.send();
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("clients", "[Staff] List all clients");
    }

}
