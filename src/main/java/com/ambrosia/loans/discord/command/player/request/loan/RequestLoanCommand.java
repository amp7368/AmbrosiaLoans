package com.ambrosia.loans.discord.command.player.request.loan;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.command.BaseSubCommand;
import com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.ErrorMessages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class RequestLoanCommand extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        DClient client = ClientQueryApi.findByDiscord(event.getUser().getIdLong());
        if (client != null && client.isBlacklisted()) {
            ErrorMessages.blacklisted().replyError(event);
            return;
        }
        RequestLoanModalType modalType = RequestLoanModalType.get(client == null);
        event.replyModal(modalType.buildModal()).queue();
    }

    @Override
    public SubcommandData getData() {
        return new SubcommandData("loan", "Request to take out a loan");
    }
}
