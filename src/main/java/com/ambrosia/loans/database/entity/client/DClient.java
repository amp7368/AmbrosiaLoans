package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.invest.DInvest;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
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
public class DClient extends Model implements ClientAccess {

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
    @OneToMany(mappedBy = "client")
    private final List<DLoan> loans = new ArrayList<>();
    @OneToMany
    private final List<DInvest> investments = new ArrayList<>();

    public DClient(String displayName) {
        this.displayName = displayName;
    }


    public Emeralds getBalance() {
        return Emeralds.of(this.balance);
    }

    public DClient setBalance(long balance) {
        this.balance = balance;
        return this;
    }

    @Override
    public DClient getEntity() {
        return this;
    }

    public long getId() {
        return id;
    }

    public List<DLoan> getLoans() {
        return loans.stream()
            .sorted(Comparator.comparing(DLoan::getStartDate))
            .toList();
    }

    public String getEffectiveName() {
        if (this.displayName != null) return this.displayName;
        String minecraft = getMinecraft(ClientMinecraftDetails::getName);
        if (minecraft != null) return minecraft;
        String discord = getDiscord(ClientDiscordDetails::getUsername);
        if (discord != null) return discord;
        return "error";
    }

    public ClientMinecraftDetails getMinecraft() {
        return minecraft;
    }

    public void setMinecraft(ClientMinecraftDetails minecraft) {
        this.minecraft = minecraft;
    }

    public DClient addAccountSnapshot(DAccountSnapshot snapshot) {
        this.accountSnapshots.add(snapshot);
        return this;
    }

    public ClientDiscordDetails getDiscord() {
        return this.discord;
    }

    public void setDiscord(ClientDiscordDetails discord) {
        this.discord = discord;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getDateCreated() {
        return this.dateCreated.toInstant();
    }
}
