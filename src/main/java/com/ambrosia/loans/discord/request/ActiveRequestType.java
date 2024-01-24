package com.ambrosia.loans.discord.request;

import apple.utilities.gson.adapter.GsonEnumTypeHolder;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.request.account.ActiveRequestAccount;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPayment;

public enum ActiveRequestType implements GsonEnumTypeHolder<ActiveRequest<?>> {
    LOAN(ActiveRequestLoan.class, "loan"),
    ACCOUNT(ActiveRequestAccount.class, "account"),
    PAYMENT(ActiveRequestPayment.class, "payment");

    private final Class<? extends ActiveRequest<?>> type;
    private final String typeId;

    ActiveRequestType(Class<? extends ActiveRequest<?>> type, String typeId) {
        this.type = type;
        this.typeId = typeId;
    }

    @Override
    public String getTypeId() {
        return this.typeId;
    }

    @Override
    public Class<? extends ActiveRequest<?>> getTypeClass() {
        return this.type;
    }
}
