package com.ambrosia.loans.database.entity.client.meta;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.meta.query.QDIsBotBlockedTimespan;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "client_meta")
public class DClientMeta extends Model {

    @Id
    protected UUID id;
    @OneToOne(mappedBy = "meta")
    protected DClient client;
    @OneToMany
    protected List<DIsBotBlockedTimespan> botHistory;
    @Nullable
    @Column
    protected Boolean isBotBlocked = null;
    @Nullable
    @Column
    protected Timestamp isBotBlockedCheckedAt;

    public DClientMeta(DClient client) {
        this.client = client;
        this.botHistory = new ArrayList<>();
        this.isBotBlockedCheckedAt = new Timestamp(System.currentTimeMillis());
    }

    public void startMarkNotBlocked(Object ignored) {
        startMarkNotBlocked();
    }


    public void startMarkBlocked(Throwable threw) {
        if (threw instanceof ErrorResponseException) {
            startMarkBlocked();
        } else if (threw instanceof Exception e) {
            DiscordLog.errorSystem(null, e);
        } else {
            DiscordModule.get().logger().error("", threw);
        }
    }

    public CompletableFuture<Void> startMarkNotBlocked() {
        return CompletableFuture.runAsync(() -> setIsBotBlockedAndSave(false), Ambrosia.get().executor());
    }

    public CompletableFuture<Void> startMarkBlocked() {
        return CompletableFuture.runAsync(() -> setIsBotBlockedAndSave(true), Ambrosia.get().executor());
    }

    private void setIsBotBlockedAndSave(boolean isBotBlocked) {
        this.refresh();

        DIsBotBlockedTimespan pastTimespan = getLatestTimespan();

        DIsBotBlockedTimespan nextTimespan;
        if (this.isBotBlocked == null) {
            // there is no history
            nextTimespan = saveFirstIsBotBlocked(isBotBlocked);
        } else if (pastTimespan == null) {
            // there's an error
            DiscordLog.errorSystem("getLatestTimespan is null, when there should be at least one...", null);
            return;
        } else if (this.isBotBlocked == isBotBlocked) {
            // past == next
            touch(pastTimespan);
            nextTimespan = pastTimespan;
        } else {
            // there is something new
            this.isBotBlocked = isBotBlocked;
            nextTimespan = updateIsBotBlockedTimespan(pastTimespan);
        }
        if (shouldLogBlocked(isBotBlocked, pastTimespan))
            tryLog(nextTimespan);
        this.refresh();
    }

    private void tryLog(DIsBotBlockedTimespan nextTimespan) {
        DiscordLog.botBlocked(client, nextTimespan.isBlocked());
        nextTimespan.markNotified().save();
    }

    /**
     * Determines whether staff should be notified based on past and next isBotBlocked states.
     *
     * <p> Decision Table:
     * <pre>
     * (past == null) && (next == false)        => NO
     * (past == null) && (next == true)         => YES
     * (past == false) && (next == false)       => NO
     * (past == false) && (next == true)        => YES
     * (past == true) && (next == false)        => YES
     * (past == true) && (next == true)         =>
     *      If last notification was:
     *          - Greater than 30 minutes ago   => YES
     *          - Less than 30 minutes ago      => NO
     * </pre>
     *
     * @param next     The next time-span's isBotBlocked value
     * @param timespan the previous timespan
     * @return {@code true} if staff should be notified, otherwise {@code false}.
     */
    private boolean shouldLogBlocked(boolean next, DIsBotBlockedTimespan timespan) {
        Boolean past = this.isBotBlocked;
        if (past == null || timespan == null) return next;
        if (!past && !next) return false;
        if (!past) return true;
        if (!next) return true;

        Duration sinceLastLog = timespan.getTimeSinceLastNotified();
        return sinceLastLog.compareTo(Duration.ofMinutes(30)) > 0;
    }

    private DIsBotBlockedTimespan updateIsBotBlockedTimespan(DIsBotBlockedTimespan timespan) {
        if (isBotBlocked == null) throw new IllegalStateException("Impossible");

        try (Transaction transaction = DB.beginTransaction()) {
            DIsBotBlockedTimespan next = new DIsBotBlockedTimespan(this, isBotBlocked);
            next.save(transaction);
            timespan.endAt(next.getLastCheckedAt())
                .save(transaction);
            save(transaction);
            transaction.commit();
            return timespan;
        }
    }

    public boolean isBotBlocked() {
        return Objects.requireNonNullElse(isBotBlocked, false);
    }

    private DIsBotBlockedTimespan saveFirstIsBotBlocked(boolean isBotBlocked) {
        this.isBotBlocked = isBotBlocked;
        this.isBotBlockedCheckedAt = new Timestamp(System.currentTimeMillis());
        try (Transaction transaction = DB.beginTransaction()) {
            DIsBotBlockedTimespan timespan = new DIsBotBlockedTimespan(this, isBotBlocked);
            timespan.save(transaction);
            save(transaction);
            transaction.commit();
            return timespan;
        }
    }

    private DIsBotBlockedTimespan touch(DIsBotBlockedTimespan timespan) {
        if (isBotBlocked == null) throw new IllegalStateException("Impossible");

        if (timespan.isBlocked() != isBotBlocked) {
            String msg = "Timespan{%s}=%b is not equal blocked in Meta{%s}=%b"
                .formatted(timespan.getId(), timespan.isBlocked(), id, isBotBlocked);
            DiscordLog.errorSystem(msg);
            return timespan;
        }
        try (Transaction transaction = DB.beginTransaction()) {
            Instant now = Instant.now();
            this.isBotBlockedCheckedAt = Timestamp.from(now);
            timespan.setLastCheckedAt(now)
                .save(transaction);
            save(transaction);
            transaction.commit();
            return timespan;
        }
    }

    @Nullable
    private DIsBotBlockedTimespan getLatestTimespan() {
        return new QDIsBotBlockedTimespan().where()
            .clientMeta.eq(this)
            .endedAt.isNull()
            .orderBy().lastCheckedAt.desc()
            .setMaxRows(1)
            .findOne();
    }
}
