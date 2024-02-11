package com.ambrosia.loans.database.entity.client.alter;

import com.ambrosia.loans.database.alter.base.AlterDBChange;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;

public abstract class AlterClient<T> extends AlterDBChange<DClient, T> {

    public AlterClient() {
    }

    public AlterClient(AlterRecordType typeId, DClient client, T previous, T current) {
        super(typeId, client.getId(), previous, current);
    }

    @Override
    public DClient getEntity() {
        return ClientQueryApi.findById(this.getEntityId());
    }
}
