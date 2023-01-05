package com.ambrosia.loans.database.client;

import io.ebean.Model;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "client")
public class DClient {

    @Id
    @Column
    @Identity(start = 1000)
    public long id;

    @Column
    @Embedded(prefix = "minecraft_")
    public ClientMinecraftDetails minecraft;
    @Column
    @Embedded(prefix = "discord_")
    public ClientDiscordDetails discord;
    @Column(unique = true, nullable = false)
    public String displayName;
    @Column(nullable = false)
    public Timestamp dateCreated;

    @DbJsonB
    public ClientMoment moment;

    public DClient(String displayName) {
        this.dateCreated = Timestamp.from(Instant.now());
        this.moment = new ClientMoment();
        this.displayName = displayName;
    }

}
