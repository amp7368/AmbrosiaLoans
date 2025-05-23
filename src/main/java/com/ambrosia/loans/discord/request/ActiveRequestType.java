package com.ambrosia.loans.discord.request;

import apple.utilities.gson.adapter.GsonEnumTypeAdapter;
import apple.utilities.gson.adapter.GsonEnumTypeHolder;
import apple.utilities.json.gson.GsonBuilderDynamic;
import apple.utilities.util.Pretty;
import com.ambrosia.loans.discord.base.request.ActiveRequest;
import com.ambrosia.loans.discord.request.account.ActiveRequestAccount;
import com.ambrosia.loans.discord.request.investment.ActiveRequestInvestment;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.discord.request.payment.ActiveRequestPayment;
import com.ambrosia.loans.discord.request.withdrawal.ActiveRequestWithdrawal;
import com.ambrosia.loans.util.InstantGsonSerializing;
import com.google.gson.Gson;
import java.time.Instant;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

public enum ActiveRequestType implements GsonEnumTypeHolder<ActiveRequest<?>> {
    LOAN(ActiveRequestLoan.class, "loan"),
    ACCOUNT(ActiveRequestAccount.class, "account"),
    PAYMENT(ActiveRequestPayment.class, "payment"),
    INVESTMENT(ActiveRequestInvestment.class, "investment"),
    WITHDRAWAL(ActiveRequestWithdrawal.class, "withdrawal");

    private final Class<? extends ActiveRequest<?>> type;
    private final String typeId;

    ActiveRequestType(Class<? extends ActiveRequest<?>> type, String typeId) {
        this.type = type;
        this.typeId = typeId;
    }

    public static Gson gson() {
        return GsonEnumTypeAdapter.register(ActiveRequestType.values(), new GsonBuilderDynamic(), ActiveRequest.class)
            .registerTypeAdapter(Instant.class, new InstantGsonSerializing())
            .create();
    }

    @Nullable
    public static ActiveRequestType fromTypeId(String typeId) {
        return Arrays.stream(values())
            .filter(type -> type.typeId.equals(typeId))
            .findAny()
            .orElse(null);
    }

    @Override
    public String getTypeId() {
        return this.typeId;
    }

    @Override
    public Class<? extends ActiveRequest<?>> getTypeClass() {
        return this.type;
    }

    public String getDisplayName() {
        return Pretty.spaceEnumWords(name());
    }
}
