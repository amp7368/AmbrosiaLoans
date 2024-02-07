package com.ambrosia.loans.migrate.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import java.sql.Timestamp;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class ImportedClient {

    private final long id;
    private final ClientMinecraftDetails minecraft;
    private final ClientDiscordDetails discord;
    private Instant dateCreated = Instant.now();
    private DClient db;

    public ImportedClient(RawClient raw) {
        this.id = raw.getId();
        this.minecraft = raw.getMinecraft();
        this.discord = raw.getDiscord();
    }

    public DClient toDB() {
        if (this.db != null) throw new IllegalStateException("#toDB() was already called for client %d!".formatted(this.id));
        this.db = new DClient(this);
        this.db.save();
        return this.db;
    }

    public long getId() {
        return this.id;
    }

    public ClientMinecraftDetails getMinecraft() {
        return minecraft;
    }

    public ClientDiscordDetails getDiscord() {
        return discord;
    }

    public void checkDateCreated(@NotNull Instant dateAction) {
        if (this.dateCreated.isAfter(dateAction)) {
            this.dateCreated = dateAction;
        }
    }

    public Timestamp getDateCreated() {
        return Timestamp.from(dateCreated);
    }

    public DClient getDB() {
        return this.db;
    }
}
