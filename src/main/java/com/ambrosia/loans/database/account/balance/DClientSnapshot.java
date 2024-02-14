package com.ambrosia.loans.database.account.balance;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "client_snapshot")
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
    private long investDelta;
    @Index
    @Column(nullable = false)
    private long investBalance;
    @Column(nullable = false)
    private long loanDelta;
    @Index
    @Column(nullable = false)
    private long loanBalance;
    @Column(nullable = false)
    private AccountEventType event;

    public DClientSnapshot(DClient client, Instant date, long investBalance, long loanBalance, long delta,
        AccountEventType event) {
        this.date = Timestamp.from(date);
        this.client = client;
        this.investBalance = investBalance;
        this.loanBalance = loanBalance;

        if (event.isLoanLike()) loanDelta = delta;
        else investDelta = delta;

        this.event = event;
    }

    public Emeralds getAccountBalance() {
        return Emeralds.of(this.investBalance + this.loanBalance);
    }

    @NotNull
    public Instant getDate() {
        return date.toInstant();
    }

    @NotNull
    public AccountEventType getEventType() {
        return this.event;
    }

    public Emeralds getDelta() {
        return Emeralds.of(this.investDelta + this.loanDelta);
    }

    @Override
    public int compareTo(@NotNull DClientSnapshot o) {
        return COMPARATOR.compare(this, o);
    }
}
