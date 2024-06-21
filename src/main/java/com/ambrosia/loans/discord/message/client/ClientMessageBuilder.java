package com.ambrosia.loans.discord.message.client;

import com.ambrosia.loans.database.entity.client.DClient;

public class ClientMessageBuilder implements ClientMessage {

    private final DClient client;

    ClientMessageBuilder(DClient client) {
        this.client = client;
    }

    @Override
    public DClient getClient() {
        return client;
    }
}
