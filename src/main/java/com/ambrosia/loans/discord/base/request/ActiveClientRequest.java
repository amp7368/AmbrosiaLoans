package com.ambrosia.loans.discord.base.request;

import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.request.ActiveRequestType;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public abstract class ActiveClientRequest<Gui extends ActiveRequestGui<?>> extends ActiveRequest<Gui> {

    protected long clientId;
    private transient DClient client;

    public ActiveClientRequest(ActiveRequestType typeId, @NotNull DClient client) {
        super(typeId, new ActiveRequestSender(client));
        this.clientId = client.getId();
        this.client = client;
    }

    public ActiveClientRequest(ActiveRequestType typeId) {
        super(typeId, null);
    }

    public DClient getClient() {
        if (client != null) {
            client.refresh();
            return client;
        }
        return this.client = ClientQueryApi.findById(clientId);
    }

    public Emeralds getBalance() {
        return getClient().getBalanceWithRecentInterest(Instant.now()).totalEmeralds();
    }
}
