package com.ambrosia.loans.discord.command.player.show.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import discord.util.dcf.gui.base.edit_message.DCFEditMessage;
import discord.util.dcf.util.DCFUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ShowLoansCommand extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        DCFUtils.get().builderDefer(event,
            (defer, ignored) -> {
                ClientGui gui = new ClientGui(client, dcf, DCFEditMessage.ofHook(defer));
                new LoanHistoryMessage(gui)
                    .addPageToGui()
                    .send();
            },
            () -> {
                client.getLoans();
                return client;
            }
        ).startDefer();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loans", "View all your past loans");
    }
}
