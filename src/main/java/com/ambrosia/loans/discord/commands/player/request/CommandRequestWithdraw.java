package com.ambrosia.loans.discord.commands.player.request;

import com.ambrosia.loans.database.transaction.TransactionType;

public class CommandRequestWithdraw extends CommandRequestCash {

    @Override
    protected int sign() {
        return -1;
    }

    @Override
    protected TransactionType transactionType() {
        return TransactionType.WITHDRAW;
    }
}
