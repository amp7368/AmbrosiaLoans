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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    public void startMarkNotBlocked() {
        Ambrosia.get().execute(() -> setIsBotBlockedAndSave(false));
    }

    public void startMarkBlocked() {
        Ambrosia.get().execute(() -> setIsBotBlockedAndSave(true));
    }

    private void setIsBotBlockedAndSave(boolean isBotBlocked) {
        this.refresh();
        if (this.isBotBlocked == null) {
            saveFirstIsBotBlocked(isBotBlocked);
            return;
        }
        DIsBotBlockedTimespan timespan = getLatestTimespan();
        if (timespan == null) {
            DiscordLog.errorSystem("getLatestTimespan is null, when there should be at least one...", null);
        } else if (this.isBotBlocked == isBotBlocked) {
            touch(timespan);
        } else {
            this.isBotBlocked = isBotBlocked;
            updateIsBotBlockedTimespan(timespan);
        }
    }

    private void updateIsBotBlockedTimespan(DIsBotBlockedTimespan timespan) {
        if (isBotBlocked == null) throw new IllegalStateException("Impossible");

        DiscordLog.botBlocked(client, isBotBlocked);
        try (Transaction transaction = DB.beginTransaction()) {
            DIsBotBlockedTimespan next = new DIsBotBlockedTimespan(this, isBotBlocked);
            next.save(transaction);
            timespan.endAt(next.getLastCheckedAt())
                .save(transaction);
            save(transaction);
            transaction.commit();
        }
    }

    private void saveFirstIsBotBlocked(boolean isBotBlocked) {
        if (isBotBlocked) DiscordLog.botBlocked(client, true);

        this.isBotBlocked = isBotBlocked;
        this.isBotBlockedCheckedAt = new Timestamp(System.currentTimeMillis());
        try (Transaction transaction = DB.beginTransaction()) {
            new DIsBotBlockedTimespan(this, isBotBlocked).save(transaction);
            save(transaction);
            transaction.commit();
        }
    }

    private void touch(DIsBotBlockedTimespan timespan) {
        if (isBotBlocked == null) throw new IllegalStateException("Impossible");

        if (timespan.isBlocked() != isBotBlocked) {
            String msg = "Timespan{%s}=%b is not equal blocked in Meta{%s}=%b"
                .formatted(timespan.getId(), timespan.isBlocked(), id, isBotBlocked);
            DiscordLog.errorSystem(msg);
            return;
        }
        try (Transaction transaction = DB.beginTransaction()) {
            Instant now = Instant.now();
            this.isBotBlockedCheckedAt = Timestamp.from(now);
            timespan.setLastCheckedAt(now)
                .save(transaction);
            save(transaction);
            transaction.commit();
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
