package com.ambrosia.loans.discord.active;

import apple.utilities.gson.adapter.GsonEnumTypeHolder;
import com.ambrosia.loans.discord.active.account.ActiveRequestAccount;
import com.ambrosia.loans.discord.active.base.ActiveRequest;
import com.ambrosia.loans.discord.active.cash.ActiveRequestCash;

public enum ActiveRequestType implements GsonEnumTypeHolder<ActiveRequest> {
    CASH(ActiveRequestCash.class, "cash"),
    ACCOUNT(ActiveRequestAccount.class, "account");

    private final Class<? extends ActiveRequest> type;
    private final String typeId;

    ActiveRequestType(Class<? extends ActiveRequest> type, String typeId) {
        this.type = type;
        this.typeId = typeId;
    }

    @Override
    public String getTypeId() {
        return this.typeId;
    }

    @Override
    public Class<? extends ActiveRequest> getTypeClass() {
        return this.type;
    }
}
