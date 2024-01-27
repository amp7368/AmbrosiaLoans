package com.ambrosia.loans.discord.request.base;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.invest.InvestApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.util.emerald.Emeralds;

public abstract class BaseActiveRequestInvest<Gui extends ActiveRequestGui<?>> extends ActiveClientRequest<Gui> {

    protected long amount;

    public BaseActiveRequestInvest(ActiveRequestType typeId) {
        super(typeId);
    }

    public BaseActiveRequestInvest(ActiveRequestType typeId, DClient client, Emeralds amount) {
        super(typeId, client);
        setRequestId();
        this.amount = amount.amount();
    }

    @Override
    public void onComplete() throws Exception {
        InvestApi.createInvestLike(this);
    }

    public Emeralds getAmount() {
        return Emeralds.of(this.amount);
    }

    public BaseActiveRequestInvest<Gui> setAmount(Emeralds amount) {
        this.amount = amount.amount();
        return this;
    }

    public abstract AccountEventType getEventType();
}
