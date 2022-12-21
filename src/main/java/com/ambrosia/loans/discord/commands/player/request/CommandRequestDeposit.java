package com.ambrosia.loans.discord.commands.player.request;

import com.ambrosia.loans.database.transaction.TransactionType;
import org.jetbrains.annotations.NotNull;

public class CommandRequestDeposit extends CommandRequestCash {

    @NotNull
    protected TransactionType transactionType() {
        return TransactionType.DEPOSIT;
    }

    protected int sign() {
        return 1;
    }
}
