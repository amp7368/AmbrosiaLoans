package com.ambrosia.loans.discord.commands.player.hisotry.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LoanHistoryCommand extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        ClientGui gui = new ClientGui(client, dcf, event::reply);
        gui.addPage(new LoanHistoryMessage(gui));
        gui.send();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loan", "View all your past loans");
    }
}
