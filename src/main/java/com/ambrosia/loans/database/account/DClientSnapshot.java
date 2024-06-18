package com.ambrosia.loans.database.account;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import org.jetbrains.annotations.NotNull;

@MappedSuperclass
public class DClientSnapshot extends Model implements Comparable<DClientSnapshot> {

    private static final Comparator<DClientSnapshot> COMPARATOR = Comparator.comparing(DClientSnapshot::getDate)
        .thenComparing(DClientSnapshot::getEventType, AccountEventType.ORDER);
    @Id
    private UUID id;
    @ManyToOne(optional = false)
    private DClient client;
    @Index
    @Column(nullable = false)
    private Timestamp date;
    @Column(nullable = false)
    private long delta;
    @Index
    @Column(nullable = false)
    private long balance;
    @Column(nullable = false, columnDefinition = "event_type")
    private AccountEventType event;

    public DClientSnapshot(DClient client, Instant date, long balance, long delta, AccountEventType event) {
        this.date = Timestamp.from(date);
        this.client = client;
        this.balance = balance;
        this.delta = delta;
        this.event = event;
    }

    @NotNull
    public Instant getDate() {
        return date.toInstant();
    }

    @NotNull
    public AccountEventType getEventType() {
        return this.event;
    }

    @NotNull
    public Emeralds getBalance() {
        return Emeralds.of(balance);
    }

    @NotNull
    public Emeralds getDelta() {
        return Emeralds.of(this.delta);
    }

    @Override
    public int compareTo(@NotNull DClientSnapshot o) {
        return COMPARATOR.compare(this, o);
    }


}
