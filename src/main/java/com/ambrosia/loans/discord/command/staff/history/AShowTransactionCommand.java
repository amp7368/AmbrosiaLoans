package com.ambrosia.loans.discord.command.staff.history;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.base.command.option.CommandOption;
import com.ambrosia.loans.discord.base.command.option.CommandOptionList;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.command.player.show.transaction.TransactionHistoryMessage;
import java.util.List;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class AShowTransactionCommand extends BaseSubCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = CommandOption.CLIENT.getRequired(event);
        if (client == null) return;
        ClientGui gui = new ClientGui(client, dcf, event::reply);
        gui.addPage(new TransactionHistoryMessage(gui));
        gui.send();
    }

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData("transaction", "[Staff] View a client's past Transactions");
        CommandOptionList.of(List.of(CommandOption.CLIENT))
            .addToCommand(command);
        return command;
    }

}
