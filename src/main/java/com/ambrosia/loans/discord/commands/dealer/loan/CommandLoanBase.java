package com.ambrosia.loans.discord.commands.dealer.loan;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.discord.base.BaseSubCommand;
import com.ambrosia.loans.discord.base.CommandOption;
import com.ambrosia.loans.discord.base.CommandOptionClient;
import kotlin.NotImplementedError;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class CommandLoanBase extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        ClientApi client = CommandOptionClient.findClientApi(event);
        if (client.isEmpty()) return;
        long conductorId = event.getUser().getIdLong();
        doCommandAction(client, event);
        client.refresh();
//        new ProfileMessage(client, display()).reply(event);
//        DiscordLog.log().operation(client, operation, event.getUser(), true);
        throw new NotImplementedError("LOL!");
    }

    protected abstract void doCommandAction(ClientApi client, SlashCommandInteractionEvent event);


    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData(commandName(), "Loan action on a profile");
        CommandOption.PROFILE_NAME.addOption(command, true);
        if (requiresOptionAmount())
            addOptionAmount(command);
        if (requiresOptionLoan())
            CommandOption.LOAN.addOption(command, true);
        return command;
    }

    protected boolean requiresOptionAmount() {
        return false;
    }

    protected boolean requiresOptionLoan() {
        return false;
    }

    protected abstract String commandName();

}
