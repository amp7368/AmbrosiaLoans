package com.ambrosia.loans.database.entity.actor;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.ClientApi.ClientQueryApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.DiscordBot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public class UserActorImpl implements UserActor {

    private final List<Future<?>> fetch = new ArrayList<>();

    private DStaffConductor conductor;
    @Nullable
    private User discord;
    @Nullable
    private DClient client;

    public UserActorImpl(Future<User> discordFetch) {
        fetch.add(CompletableFuture.runAsync(() -> fetchDiscord(discordFetch)));
    }

    public UserActorImpl(DStaffConductor conductor) {
        this.conductor = conductor;
        fetch.add(CompletableFuture.runAsync(this::fetchFromConductor));
    }

    public UserActorImpl(@Nullable DClient client) {
        this.client = client;
        fetch.add(CompletableFuture.runAsync(this::fetchFromClient));
    }

    private void fetchDiscord(Future<User> discordFetch) {
        if (discordFetch == null) return;
        try {
            this.discord = discordFetch.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        this.client = ClientQueryApi.findByDiscord(discord.getIdLong());
    }

    private void fetchFromConductor() {
        if (this.conductor == null) return;
        this.client = conductor.getClient();
        fetchFromClient();
    }

    private void fetchFromClient() {
        if (this.client == null) return;
        ClientDiscordDetails details = client.getDiscord();
        if (details == null) return;
        this.discord = DiscordBot.jda().getUserById(details.getDiscordId());
    }

    @Override
    public void fetch() {
        try {
            for (Future<?> fetch : fetch) {
                fetch.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Ambrosia.get().logger().error("Cannot gather data for UserActor", e);
        }
    }

    @Override
    @Nullable
    public Long getDiscordIdLong() {
        if (this.discord != null) return this.discord.getIdLong();
        return null;
    }

    @Override
    @Nullable
    public String getName() {
        if (this.client != null) return "@" + client.getEffectiveName();
        if (this.discord != null) return "@" + discord.getEffectiveName();
        if (this.conductor != null) return "@" + conductor.getName();
        return null;
    }

    @Override
    @Nullable
    public String getActorUrl() {
        if (this.discord != null)
            return this.discord.getAvatarUrl();
        if (this.client != null)
            return this.client.getDiscord(ClientDiscordDetails::getAvatarUrl);
        return null;
    }

    @Override
    public @Nullable DClient getClient() {
        return client;
    }
}
