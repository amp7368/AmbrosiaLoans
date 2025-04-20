package com.ambrosia.loans.database.entity.client.username;

import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.Model;
import io.ebean.annotation.DbJson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "name_history")
public class DNameHistory extends Model {

    @Id
    private UUID id;

    @ManyToOne
    private DClient client;
    @Column
    private Timestamp firstUsed;
    @Column
    private Timestamp lastUsed;
    @Column
    private NameHistoryType type;
    @Nullable
    @Column
    private String name;
    @Nullable
    @DbJson
    private Object nameObject;

    public DNameHistory(DClient client, NameHistoryType type, @Nullable String name, @Nullable Object nameObject) {
        this.client = client;
        this.type = type;
        this.firstUsed = Timestamp.from(Instant.now());
        this.name = name;
        this.nameObject = nameObject;
    }

    public Instant getFirstUsed() {
        if (firstUsed == null) return Instant.EPOCH;
        return firstUsed.toInstant();
    }

    public boolean isCurrent() {
        return this.lastUsed == null;
    }

    @NotNull
    public Instant getLastUsed() {
        if (lastUsed == null) return Instant.MAX;
        return lastUsed.toInstant();
    }

    public NameHistoryType getType() {
        return this.type;
    }

    public boolean isUsedAt(Instant date) {
        if (getFirstUsed().isAfter(date)) return false;
        return date.isBefore(getLastUsed());
    }

    public void retireName(Instant now) {
        this.lastUsed = Timestamp.from(now);
    }

    public DClient getClient() {
        return client;
    }

    @Nullable
    public String getName() {
        return this.name;
    }
}
