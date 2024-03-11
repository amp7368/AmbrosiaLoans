package com.ambrosia.loans.discord.request.payment;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.system.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Optional;

public class ActiveRequestPayment extends ActiveClientRequest<ActiveRequestPaymentGui> {

    protected long paymentAmount;
    protected Instant timestamp;

    public ActiveRequestPayment() {
        super(ActiveRequestType.PAYMENT);
    }

    public ActiveRequestPayment(DClient client, Emeralds payment, Instant timestamp) {
        super(ActiveRequestType.PAYMENT, client);
        setRequestId();
        this.paymentAmount = payment.amount();
        this.timestamp = timestamp;
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

    public Emeralds getBalance() throws BadDateAccessException {
        return super.getBalance(timestamp);
    }
}
