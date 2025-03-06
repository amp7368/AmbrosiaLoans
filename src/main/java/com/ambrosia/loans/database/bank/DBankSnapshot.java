package com.ambrosia.loans.database.bank;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bank_snapshot")
public class DBankSnapshot extends Model {

    @Id
    protected UUID id;
    @Index
    @Column(nullable = false)
    protected AccountEventType type;
    @Index
    @Column(nullable = false)
    private Timestamp date;
    @Column(nullable = false)
    private long balance;
    @Column(nullable = false)
    private long delta;

    public DBankSnapshot(AccountEventType type, Timestamp date, long balance, long delta) {
        this.type = type;
        this.date = date;
        this.balance = balance;
        this.delta = delta;
    }

    public long getBalanceAmount() {
        return this.balance;
    }

    public Emeralds getBalance() {
        return Emeralds.of(this.balance);
    }

    public Instant getDate() {
        return date.toInstant();
    }
}
