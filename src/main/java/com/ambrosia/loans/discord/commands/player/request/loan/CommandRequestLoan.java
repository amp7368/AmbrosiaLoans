package com.ambrosia.loans.discord.commands.player.request.loan;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.client.BaseClientSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandRequestLoan extends BaseClientSubCommand {

    @Override
    public void onClientCommand(SlashCommandInteractionEvent event, DClient client) {
        RequestLoanModalType modalType = RequestLoanModalType.get(client == null);
        event.replyModal(modalType.buildModal()).queue();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loan", "Request to take out a loan");
    }
}
