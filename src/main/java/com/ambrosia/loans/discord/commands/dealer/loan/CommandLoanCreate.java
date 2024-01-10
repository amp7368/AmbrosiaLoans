package com.ambrosia.loans.discord.commands.dealer.loan;

import com.ambrosia.loans.database.entity.client.ClientApi;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandLoanCreate extends CommandLoanBase {

    @Override
    protected void doCommandAction(ClientApi client, SlashCommandInteractionEvent event) {

    }

    @Override
    protected String commandName() {
        return "create";
    }
}
