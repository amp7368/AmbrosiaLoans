package com.ambrosia.loans.database.entity.client.alter;

import com.ambrosia.loans.database.alter.change.AlterDBChange;
import com.ambrosia.loans.database.alter.type.AlterChangeType;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;

public abstract class AlterClient<T> extends AlterDBChange<DClient, T> {

    public AlterClient() {
    }

    public AlterClient(AlterChangeType typeId, DClient client, T previous, T current) {
        super(typeId, client.getId(), previous, current);
    }

    @Override
    public String getEntityType() {
        return "Client";
    }

    @Override
    public DClient getEntity() {
        return ClientQueryApi.findById(this.getEntityId());
    }
}
