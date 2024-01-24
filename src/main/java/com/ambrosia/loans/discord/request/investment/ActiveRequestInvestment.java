package com.ambrosia.loans.discord.request.investment;

import com.ambrosia.loans.database.account.event.invest.InvestApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.exception.BadDateAccessException;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;

public class ActiveRequestInvestment extends ActiveClientRequest<ActiveRequestInvestmentGui> {

    protected long investmentAmount;
    protected Instant timestamp;

    public ActiveRequestInvestment() {
        super(ActiveRequestType.INVESTMENT);
    }

    public ActiveRequestInvestment(DClient client, Emeralds amount, Instant timestamp) {
        super(ActiveRequestType.INVESTMENT, client);
        setRequestId();
        this.investmentAmount = amount.amount();
        this.timestamp = timestamp;
    }

    @Override
    public void onComplete() throws Exception {
        InvestApi.createInvestment(this);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ActiveRequestInvestment setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public ActiveRequestInvestmentGui load() {
        return new ActiveRequestInvestmentGui(messageId, this);
    }

    public Emeralds getInvestment() {
        return Emeralds.of(this.investmentAmount);
    }

    public ActiveRequestInvestment setInvestment(Emeralds investment) {
        this.investmentAmount = investment.amount();
        return this;
    }

    public Emeralds getBalance() throws BadDateAccessException {
        return super.getBalance(timestamp);
    }
}
