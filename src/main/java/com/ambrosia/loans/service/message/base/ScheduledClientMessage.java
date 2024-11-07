package com.ambrosia.loans.service.message.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.discord.message.client.ClientMessage;
import com.ambrosia.loans.service.message.MessageDestination;
import com.ambrosia.loans.service.message.ScheduledMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ScheduledClientMessage<T extends SentClientMessage> implements ScheduledMessage, ClientMessage {

    private final DClient client;
    private final Instant notificationTime;
    private final List<MessageDestination<T>> destinations = new ArrayList<>();

    public ScheduledClientMessage(DClient client, Instant notificationTime) {
        this.client = client;
        this.notificationTime = notificationTime;
    }

    public void addDestination(MessageDestination<T> destination) {
        destinations.add(destination);
    }

    public DClient getClient() {
        return client;
    }

    @Override
    public Instant getNotificationTime() {
        return notificationTime;
    }

    @Override
    public boolean filterDev() {
        return client.getId() == 100 || client.getId() == 101;
    }

    @Override
    public CompletableFuture<Void> send() {
        ClientDiscordDetails discord = client.getDiscord();
        if (discord == null) {
            String msg = "Discord is null for client %s{%s}".formatted(client.getEffectiveName(), client.getId());
            throw new IllegalStateException(msg);
        }
        T sent = makeSentMessage();
        sent.setDescription(getDescription());
        return sent.sendFirst(sent, destinations);
    }

    @Override
    public String toString() {
        return getClient().getEffectiveName() + "'s message";
    }

    public abstract String getDescription();

    protected abstract T makeSentMessage();
}
