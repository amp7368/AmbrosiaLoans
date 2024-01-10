package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.log.DAccountLog;
import com.ambrosia.loans.database.log.loan.DLoan;
import com.ambrosia.loans.database.simulate.DAccountSimulation;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
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
    @OneToOne
    private final DAccountLog accountLog;
    @OneToOne
    private final DAccountSimulation accountSimulation;

    public DClient(String displayName) {
        this.displayName = displayName;
        this.accountLog = new DAccountLog(this);
        this.accountSimulation = new DAccountSimulation(this);
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
        return this.accountLog.getLoans();
    }

    public DAccountLog getAccountLog() {
        return this.accountLog;
    }

    public DAccountSimulation getAccountSimulation() {
        return this.accountSimulation;
    }
}
