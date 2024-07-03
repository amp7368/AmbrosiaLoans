package com.ambrosia.loans.database.entity.client.meta;

import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.jetbrains.annotations.Nullable;

@Embeddable
public class ClientMinecraftDetails {

    @Column(unique = true)
    private UUID uuid;
    @Index
    @Column(unique = true)
    private String username;
    @Column
    private Timestamp lastUpdated;

    private ClientMinecraftDetails(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.lastUpdated = Timestamp.from(Instant.now());
    }

    @Nullable
    public static ClientMinecraftDetails fromUsername(String username) {
        if (username == null) return null;

        return UpdateClientMinecraftHook.fromUsernameNow(username);
    }

    public static ClientMinecraftDetails fromRaw(UUID uuid, String username) {
        return new ClientMinecraftDetails(uuid, username);
    }

    public Instant getLastUpdated() {
        if (lastUpdated == null)
            return Instant.EPOCH;
        return lastUpdated.toInstant();
    }

    public void resetLastUpdated() {
        this.lastUpdated = Timestamp.from(Instant.now());
    }

    public String skinUrl() {
        return "https://mc-heads.net/head/" + this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void update(ClientMinecraftDetails other) {
        this.uuid = other.uuid;
        this.username = other.username;
        this.lastUpdated = Timestamp.from(Instant.now());
    }
}
