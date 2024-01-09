package com.ambrosia.loans.database.client;

import com.ambrosia.loans.database.loan.DLoan;
import com.ambrosia.loans.database.loan.collateral.DCollateral;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import javax.persistence.CascadeType;
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
    @OneToMany
    private List<DLoan> loans;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DCollateral> collateral;

    public DClient(String displayName) {
        this.dateCreated = Timestamp.from(Instant.now());
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

    public ClientApi api() {
        return new ClientApi(this);
    }

    public List<DLoan> getLoans() {
        return this.loans.stream().sorted(Comparator.comparing(DLoan::getStartDate)).toList();
    }
}
