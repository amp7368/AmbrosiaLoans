package com.ambrosia.loans.discord.request.investment;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;

public class ActiveRequestInvestment extends BaseActiveRequestInvest<ActiveRequestInvestmentGui> {

    public ActiveRequestInvestment() {
        super(ActiveRequestType.INVESTMENT);
    }

    public ActiveRequestInvestment(DClient client, Emeralds amount) {
        super(ActiveRequestType.INVESTMENT, client, amount);
    }

    @Override
    public ActiveRequestInvestmentGui load() {
        return new ActiveRequestInvestmentGui(messageId, this);
    }

    @Override
    public AccountEventType getEventType() {
        return AccountEventType.INVEST;
    }
}
