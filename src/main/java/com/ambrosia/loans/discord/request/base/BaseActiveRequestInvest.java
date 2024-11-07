package com.ambrosia.loans.discord.request.base;

import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.base.AccountEventApi;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.alter.AlterRecordApi.AlterQueryApi;
import com.ambrosia.loans.database.alter.create.DAlterCreate;
import com.ambrosia.loans.database.alter.type.AlterCreateType;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.request.ActiveClientRequest;
import com.ambrosia.loans.discord.base.request.ActiveRequestGui;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.util.emerald.Emeralds;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public DAlterCreate onComplete() throws Exception {
        AccountEvent event = AccountEventApi.createInvestLike(this);
        AlterCreateType type = event.getEventType().getAlterCreateType();
        DiscordLog.createInvestLike(event, UserActor.of(getEndorserUser()));
        return AlterQueryApi.findCreateByEntityId(event.getId(), type);
    }

    public boolean shouldDeferOnComplete() {
        return true;
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
