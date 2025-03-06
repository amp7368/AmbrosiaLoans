package com.ambrosia.loans.database.entity.client.meta;

import io.ebean.Model;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bot_blocked_timespan")
public class DIsBotBlockedTimespan extends Model {

    @Id
    private UUID id;
    @ManyToOne
    private DClientMeta clientMeta;
    @Column
    private boolean isBlocked;
    @Column
    private Timestamp startedAt;
    @Column
    private Timestamp lastCheckedAt;
    @Column
    private Timestamp endedAt;

    public DIsBotBlockedTimespan(DClientMeta clientMeta, boolean isBlocked) {
        this.clientMeta = clientMeta;
        this.isBlocked = isBlocked;
        this.startedAt = Timestamp.from(Instant.now());
        this.lastCheckedAt = this.startedAt;
    }

    public Instant getStartedAt() {
        if (startedAt == null) return Instant.now();
        return startedAt.toInstant();
    }

    public DClientMeta getClientMeta() {
        return clientMeta;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public Instant getLastCheckedAt() {
        return this.lastCheckedAt.toInstant();
    }

    public DIsBotBlockedTimespan setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = Timestamp.from(lastCheckedAt);
        return this;
    }

    public DIsBotBlockedTimespan endAt(Instant end) {
        this.endedAt = Timestamp.from(end);
        return this;
    }

    public UUID getId() {
        return this.id;
    }
}
