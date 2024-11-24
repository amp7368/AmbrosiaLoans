package com.ambrosia.loans.service.message.base;

import apple.utilities.gson.adapter.GsonEnumTypeAdapter;
import apple.utilities.gson.adapter.GsonEnumTypeHolder;
import apple.utilities.json.gson.GsonBuilderDynamic;
import com.ambrosia.loans.service.message.dms.LoanFreezeDM;
import com.ambrosia.loans.service.message.dms.ManualClientMessage;
import com.ambrosia.loans.service.message.loan.LoanReminderMessage;
import com.google.gson.Gson;

public enum SentClientMessageType implements GsonEnumTypeHolder<SentClientMessage> {

    LOAN_REMINDER("LOAN_REMINDER", LoanReminderMessage.class),
    LOAN_FREEZE("LOAN_FREEZE", LoanFreezeDM.class),
    MANUAL("MANUAL", ManualClientMessage.class);

    private static Gson GSON;
    private final String typeId;
    private final Class<? extends SentClientMessage> type;

    SentClientMessageType(String typeId, Class<? extends SentClientMessage> type) {
        this.typeId = typeId;
        this.type = type;
    }

    public static Gson gson() {
        if (GSON != null) return GSON;
        GsonBuilderDynamic gson = new GsonBuilderDynamic();
        return GSON = GsonEnumTypeAdapter.register(values(), gson, SentClientMessage.class).create();
    }

    @Override
    public String getTypeId() {
        return typeId;
    }

    @Override
    public Class<? extends SentClientMessage> getTypeClass() {
        return type;
    }
}
