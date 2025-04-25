package com.ambrosia.loans.discord.command.staff.list.client;

import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ListClientsCommand extends BaseSubCommand {

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("clients", "[Staff] List all clients");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        ListClientsGui gui = new ListClientsGui(dcf, event::reply);
        gui.addPage(new ListClientsPage(gui));
        gui.send();
    }

}
