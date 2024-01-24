package com.ambrosia.loans.discord.request.payment;

import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestSender;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Optional;

public class ActiveRequestPayment extends ActiveRequest<ActiveRequestPaymentGui> {

    protected long paymentAmount;
    protected Instant timestamp;
    protected long clientId;
    protected transient DClient client;

    public ActiveRequestPayment() {
        super(ActiveRequestType.PAYMENT, null);
    }

    public ActiveRequestPayment(DClient client, Emeralds payment, Instant timestamp) {
        super(ActiveRequestType.PAYMENT, new ActiveRequestSender(client));
        setRequestId();
        this.paymentAmount = payment.amount();
        this.timestamp = timestamp;
        this.clientId = client.getId();
        this.client = client;
    }

    @Override
    public void onComplete() throws Exception {
        Optional<DLoan> loan = client.getActiveLoan();
        if (loan.isEmpty())
            throw new IllegalStateException("Client %s does not have any active loans!".formatted(client.getEffectiveName()));
        loan.get().makePayment(this);
    }

    @Override
    public ActiveRequestPaymentGui load() {
        return new ActiveRequestPaymentGui(messageId, this);
    }

    public Emeralds getPayment() {
        return Emeralds.of(this.paymentAmount);
    }

    public ActiveRequestPayment setPayment(Emeralds paymentAmount) {
        this.paymentAmount = paymentAmount.amount();
        return this;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public ActiveRequestPayment setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public DClient getClient() {
        if (client != null) return client;
        return this.client = ClientQueryApi.findById(clientId);
    }

    public Emeralds getBalance() throws BadDateAccessException {
        DClient client = getClient();
        if (!client.shouldGetAtTimestamp(timestamp))
            throw new BadDateAccessException(timestamp);
        return client.getBalance(timestamp);
    }
}
