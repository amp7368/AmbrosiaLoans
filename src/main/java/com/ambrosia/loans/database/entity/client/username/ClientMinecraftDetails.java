package com.ambrosia.loans.database.entity.client.username;

import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
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
    private transient DClient client;

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

    public ClientMinecraftDetails setClient(DClient client) {
        this.client = client;
        return this;
    }

    public Instant getLastUpdated() {
        if (lastUpdated == null)
            return Instant.EPOCH;
        return lastUpdated.toInstant();
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

    public boolean isNewName(ClientMinecraftDetails other) {
        if (!Objects.equals(this.uuid, other.uuid)) return true;
        return !Objects.equals(this.username, other.username);
    }

    public ClientMinecraftDetails updated() {
        return new ClientMinecraftDetails(this.uuid, this.username);
    }

    public Object json() {
        return Map.of(
            "uuid", uuid,
            "username", username
        );
    }

    public void setAll(ClientMinecraftDetails minecraft) {
        this.uuid = minecraft.uuid;
        this.username = minecraft.username;
        this.lastUpdated = minecraft.lastUpdated;
    }
}
