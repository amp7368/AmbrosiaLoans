package com.ambrosia.loans.database.simulate;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.simulate.snapshot.DAccountSnapshot;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
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

@Entity
@Table(name = "account_sim")
public class DAccountSimulation extends Model {

    @Id
    private UUID id;

    @OneToOne(optional = false, mappedBy = "accountSimulation")
    private final DClient client;

    @Column
    private long balance = 0;

    @OneToMany
    private final List<DAccountSnapshot> snapshots = new ArrayList<>();

    public DAccountSimulation(DClient client) {
        this.client = client;
    }

    public DAccountSnapshot updateBalance(long amount, Instant timestamp) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAccountSnapshot snapshot = updateBalance(amount, timestamp, transaction);
            transaction.commit();
            return snapshot;
        }
    }

    public DAccountSnapshot updateBalance(long amount, Instant timestamp, Transaction transaction) {
        this.balance += amount;
        DAccountSnapshot snapshot = new DAccountSnapshot(this, timestamp, balance, amount);
        this.snapshots.add(snapshot);
        snapshot.save(transaction);
        this.save(transaction);
        return snapshot;
    }

    public long getBalance() {
        return this.balance;
    }
}
