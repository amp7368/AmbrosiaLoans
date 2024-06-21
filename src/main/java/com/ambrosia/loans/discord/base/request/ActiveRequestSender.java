package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.message.client.ClientMessage;

public class ActiveRequestSender implements ClientMessage {

    private long clientId;
    private transient DClient client;

    public ActiveRequestSender(DClient client) {
        if (client == null) {
            this.clientId = -1;
            return;
        }
        this.clientId = client.getId();
        this.client = client;
    }

    public ActiveRequestSender() {
    }

    @Override
    public DClient getClient() {
        if (client != null) return client;
        return client = ClientQueryApi.findById(this.clientId);
    }

    public void setClient(DClient client) {
        this.client = client;
    }

}
