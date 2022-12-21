package com.ambrosia.loans.discord.commands.dealer.cash;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.transaction.DTransaction;
import com.ambrosia.loans.database.transaction.TransactionApi;
import com.ambrosia.loans.database.transaction.TransactionType;
import com.ambrosia.loans.discord.base.BaseSubCommand;
import com.ambrosia.loans.discord.base.CommandOption;
import com.ambrosia.loans.discord.base.CommandOptionClient;
import com.ambrosia.loans.discord.commands.player.profile.ProfileMessage;
import com.ambrosia.loans.discord.log.DiscordLog;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

public abstract class CommandOperation extends BaseSubCommand {

    @Override
    public void onCheckedCommand(SlashCommandInteractionEvent event) {
        if (this.isBadPermission(event)) return;
        @Nullable Integer amount = findOptionAmount(event);
        if (amount == null) return;
        DClient client = CommandOptionClient.findClient(event);
        if (client == null) return;
        long conductorId = event.getUser().getIdLong();
        int change = sign() * amount;
        DTransaction operation = TransactionApi.createTransaction(conductorId, client, change, operationReason());
        DClient updated = ClientApi.findById(client.id).client;
        if (updated == null) throw new IllegalStateException(client.id + " is not a valid client!");
        new ProfileMessage(updated, operation.display()).reply(event);
        DiscordLog.log().operation(updated, operation, event.getUser(), true);
    }


    protected abstract int sign();

    @Override
    public boolean isOnlyEmployee() {
        return true;
    }

    @Override
    public SubcommandData getData() {
        SubcommandData command = new SubcommandData(commandName(), "Deposits credits to a profile");
        CommandOption.PROFILE_NAME.addOption(command, true);
        addOptionAmount(command);
        return command;
    }

    protected abstract MessageEmbed successMessage(DClient client, int amount);

    protected abstract TransactionType operationReason();

    protected abstract String commandName();

}
