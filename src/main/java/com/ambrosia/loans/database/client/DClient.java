package com.ambrosia.loans.database.client;

import com.ambrosia.loans.database.loan.DLoan;
import io.ebean.Model;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.Identity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "client")
public class DClient extends Model implements ClientAccess<DClient> {

    @Id
    @Column
    @Identity(start = 1000)
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
    Timestamp dateCreated;
    @DbJsonB
    ClientMoment moment;
    @OneToMany
    private List<DLoan> loan;

    public DClient(String displayName) {
        this.dateCreated = Timestamp.from(Instant.now());
        this.moment = new ClientMoment();
        this.displayName = displayName;
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

    @Override
    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public void setMoment(ClientMoment moment) {
        this.moment = moment;
    }
}
