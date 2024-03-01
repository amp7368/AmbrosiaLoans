package com.ambrosia.loans.discord.request.withdrawal;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;

public class ActiveRequestWithdrawal extends BaseActiveRequestInvest<ActiveRequestWithdrawalGui> {

    public ActiveRequestWithdrawal() {
        super(ActiveRequestType.WITHDRAWAL);
    }

    public ActiveRequestWithdrawal(DClient client, Emeralds amount) {
        super(ActiveRequestType.WITHDRAWAL, client, amount);
    }

    @Override
    public ActiveRequestWithdrawalGui load() {
        return new ActiveRequestWithdrawalGui(messageId, this);
    }

    @Override
    public AccountEventType getEventType() {
        return AccountEventType.WITHDRAWAL;
    }
}
