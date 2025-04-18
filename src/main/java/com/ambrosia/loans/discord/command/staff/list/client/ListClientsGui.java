package com.ambrosia.loans.discord.command.staff.list.client;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import discord.util.dcf.DCF;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import discord.util.dcf.gui.base.gui.DCFGui;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ListClientsGui extends DCFGui {

    private final List<LoadingClient> clients;
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean isLoaded = false;

    public ListClientsGui(DCF dcf, GuiReplyFirstMessage createFirstMessage) {
        super(dcf, createFirstMessage);
        this.clients = ClientQueryApi.findALl().stream()
            .map(LoadingClient::new)
            .toList();
        Ambrosia.get().execute(this::load);
    }

    public List<LoadingClient> getClients() {
        return clients;
    }

    private void load() {
        // stuff to make listeners.forEach able to run outside the
        // for loop, while still progressing through the for loop.
        while (true) {
            Optional<LoadingClient> client = getClients().stream()
                .filter(Predicate.not(LoadingClient::isLoaded))
                .findAny();
            client.ifPresent(LoadingClient::load);

            synchronized (listeners) {
                listeners.forEach(Runnable::run);
            }
            if (client.isEmpty()) break;
        }
        synchronized (listeners) {
            listeners.clear();
            isLoaded = true;
        }
    }

    public void addListener(Runnable onChange) {
        synchronized (listeners) {
            if (isLoaded) return;
            this.listeners.add(onChange);
        }
    }
}
