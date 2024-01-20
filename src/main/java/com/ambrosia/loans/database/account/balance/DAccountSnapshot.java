package com.ambrosia.loans.database.account.balance;

import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import io.ebean.Model;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "account_sim_snapshot")
public class DAccountSnapshot extends Model {

    @Id
    private UUID id;
    @ManyToOne(optional = false)
    private DClient client;
    @Index
    @Column(nullable = false)
    private Timestamp date;
    @Column(nullable = false)
    private long accountDelta;
    @Column(nullable = false)
    private long accountBalance;
    @Column(nullable = false)
    private AccountEventType event;

    public DAccountSnapshot(DClient client, Instant date, long accountBalance, long accountDelta, AccountEventType event) {
        this.date = Timestamp.from(date);
        this.client = client;
        this.accountBalance = accountBalance;
        this.accountDelta = accountDelta;
        this.event = event;
    }
}
