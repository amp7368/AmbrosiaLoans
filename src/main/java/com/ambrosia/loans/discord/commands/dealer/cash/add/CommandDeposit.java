package com.ambrosia.loans.discord.commands.dealer.cash.add;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.transaction.TransactionType;
import com.ambrosia.loans.discord.commands.dealer.cash.CommandOperation;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

public class CommandDeposit extends CommandOperation {

    @Override
    protected MessageEmbed successMessage(DClient client, int amount) {
        return this.success(
            String.format("%s deposits %d credits into their account\nNew balance: %d", client.displayName, amount,
                client.moment.emeraldsInvested));
    }

    @Override
    protected TransactionType operationReason() {
        return TransactionType.DEPOSIT;
    }

    @NotNull
    protected String commandName() {
        return "deposit";
    }

    protected int sign() {
        return 1;
    }
}
