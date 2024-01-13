package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.invest.DInvest;
import com.ambrosia.loans.database.log.loan.DLoan;
import com.ambrosia.loans.database.simulate.snapshot.DAccountSnapshot;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "client")
public class DClient extends Model implements ClientAccess<DClient> {

    @Id
    @Column
    @Identity(start = 100)
    long id;
    @Column
    @Embedded(prefix = "minecraft_")
    ClientMinecraftDetails minecraft;
    @Column
    @Embedded(prefix = "discord_")
    ClientDiscordDetails discord;
    @Column(unique = true, nullable = false)
    String displayName;
    @Column(nullable = false)
    final Timestamp dateCreated = Timestamp.from(Instant.now());

    @Column
    private long balance = 0;
    @OneToMany
    private final List<DAccountSnapshot> accountSnapshots = new ArrayList<>();
    @OneToMany
    private final List<DLoan> loans = new ArrayList<>();
    @OneToMany
    private final List<DInvest> investments = new ArrayList<>();

    public DClient(String displayName) {
        this.displayName = displayName;
    }

    public DAccountSnapshot updateBalance(long amount, Instant timestamp, AccountEventType eventType) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAccountSnapshot snapshot = updateBalance(amount, timestamp, eventType, transaction);
            transaction.commit();
            return snapshot;
        }
    }

    public DAccountSnapshot updateBalance(long amount, Instant timestamp, AccountEventType eventType, Transaction transaction) {
        this.balance += amount;
        DAccountSnapshot snapshot = new DAccountSnapshot(this, timestamp, balance, amount, eventType);
        this.accountSnapshots.add(snapshot);
        snapshot.save(transaction);
        this.save(transaction);
        return snapshot;
    }

    public Emeralds getBalance() {
        return Emeralds.of(this.balance);
    }


    @Override
    public DClient getEntity() {
        return this;
    }

    @Override
    public DClient getSelf() {
        return null;
    }

    @Override
    public void setMinecraft(ClientMinecraftDetails minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void setDiscord(ClientDiscordDetails discord) {
        this.discord = discord;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ClientApi api() {
        return new ClientApi(this);
    }

    public List<DLoan> getLoans() {
        return loans.stream()
            .sorted(Comparator.comparing(DLoan::getStartDate))
            .toList();
    }
}
