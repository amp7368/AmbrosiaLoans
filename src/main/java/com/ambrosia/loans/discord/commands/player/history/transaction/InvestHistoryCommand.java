package com.ambrosia.loans.discord.commands.player.history.transaction;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class InvestHistoryCommand extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        ClientGui gui = new ClientGui(client, dcf, event::reply);
        gui.addPage(new TransactionHistoryMessage(gui));
        gui.send();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("transaction", "View all your past Transactions");
    }
}
