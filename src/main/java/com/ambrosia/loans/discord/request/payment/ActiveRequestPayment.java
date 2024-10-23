package com.ambrosia.loans.discord.request.payment;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.system.log.DiscordLogBuilder;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class ActiveRequestPayment extends ActiveClientRequest<ActiveRequestPaymentGui> {

    protected long paymentAmount;
    protected Instant timestamp;
    protected long loanId;

    public ActiveRequestPayment() {
        super(ActiveRequestType.PAYMENT);
    }

    public ActiveRequestPayment(DClient client, long loanId, Emeralds payment, Instant timestamp) {
        super(ActiveRequestType.PAYMENT, client);
        this.loanId = loanId;
        setRequestId();
        this.paymentAmount = payment.amount();
        this.timestamp = timestamp;
    }

    @Nullable
    @Override
    public DAlterCreate onComplete() throws Exception {
        Optional<DLoan> loan = getClient().getActiveLoan();
        if (loan.isEmpty())
            throw new IllegalStateException("Client %s does not have any active loans!".formatted(getClient().getEffectiveName()));
        DLoanPayment payment = loan.get().makePayment(this);
        DiscordLogBuilder.createPayment(payment, UserActor.of(getEndorserUser()));
        return AlterQueryApi.findCreateByEntityId(payment.getId(), AlterCreateType.PAYMENT);
    }

    public boolean shouldDeferOnComplete() {
        return true;
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

    public DLoan getLoan() {
        return LoanQueryApi.findById(this.loanId);
    }
}
