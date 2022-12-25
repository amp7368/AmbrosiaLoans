package com.ambrosia.loans.discord.commands.player.request.loan;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.discord.base.BaseSubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandRequestLoan extends BaseSubCommand {

    @Override
    protected void onCheckedCommand(SlashCommandInteractionEvent event) {
        ClientApi client = ClientApi.findByDiscord(event.getUser().getIdLong());
        if (client.isEmpty()) {
            errorRegisterWithStaff(event);
            return;
        }
        event.replyModal(RequestLoanModalType.get().buildModal()).queue();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loan", "Request to take out a loan");
    }
}
