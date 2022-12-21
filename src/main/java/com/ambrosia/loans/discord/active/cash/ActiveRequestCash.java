package com.ambrosia.loans.discord.active.cash;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.transaction.TransactionApi;
import com.ambrosia.loans.database.transaction.TransactionType;
import com.ambrosia.loans.discord.active.ActiveRequestType;
import com.ambrosia.loans.discord.active.base.ActiveRequest;
import com.ambrosia.loans.discord.active.base.ActiveRequestSender;
import io.ebean.DB;
import net.dv8tion.jda.api.entities.Member;

public class ActiveRequestCash extends ActiveRequest<ActiveRequestCashGui> {

    private final int amount;
    private final long clientId;

    private final TransactionType transactionType;

    public ActiveRequestCash(Member sender, DClient client, int amount, TransactionType transactionType, long clientId) {
        super(ActiveRequestType.CASH.getTypeId(), new ActiveRequestSender(sender, client));
        this.amount = amount;
        this.transactionType = transactionType;
        this.clientId = clientId;
    }

    @Override
    public ActiveRequestCashGui load() {
        return new ActiveRequestCashGui(messageId, this);
    }

    public void onApprove() {
        TransactionApi.createTransaction(this.getEndorserId(), DB.getDefault().reference(DClient.class,clientId), amount, transactionType);
    }

    @Override
    public void onComplete() {
    }

    public TransactionType transactionType() {
        return this.transactionType;
    }

    public int getAmount() {
        return amount;
    }

}
