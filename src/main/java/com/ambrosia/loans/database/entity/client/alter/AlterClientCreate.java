package com.ambrosia.loans.database.entity.client.alter;

import com.ambrosia.loans.database.alter.base.AlterDBCreate;
import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;

public class AlterClientCreate extends AlterDBCreate<DClient> {

    public AlterClientCreate() {

    }

    public AlterClientCreate(DClient client) {
        super(AlterRecordType.CLIENT_CREATE, client, client.getId());
    }

    @Override
    public DClient getEntity() {
        return ClientQueryApi.findById(this.getEntityId());
    }

    @Override
    protected Class<DClient> entityClass() {
        return DClient.class;
    }
}
