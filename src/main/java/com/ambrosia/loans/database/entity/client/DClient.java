package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.invest.DInvest;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.balance.ClientBalance;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.Identity;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
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
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "client")
public class DClient extends Model implements ClientAccess {

    @Id
    @Column
    @Identity(start = 100)
    private long id;
    @Column
    @Embedded(prefix = "minecraft_")
    private ClientMinecraftDetails minecraft;
    @Column
    @Embedded(prefix = "discord_")
    private ClientDiscordDetails discord;
    @Column(unique = true, nullable = false)
    private String displayName;
    @Column(nullable = false)
    private final Timestamp dateCreated = Timestamp.from(Instant.now());

    @Column
    @Embedded(prefix = "balance_")
    private final ClientBalance balance = new ClientBalance();
    @OneToMany
    private final List<DAccountSnapshot> accountSnapshots = new ArrayList<>();
    @OneToMany(mappedBy = "client")
    private final List<DLoan> loans = new ArrayList<>();
    @OneToMany
    private final List<DInvest> investments = new ArrayList<>();

    public DClient(String displayName) {
        this.displayName = displayName;
    }


    public BalanceWithInterest getBalanceWithInterest(Instant currentTime) {
        Emeralds balance = this.balance.getAmount();
        Emeralds interestAsNegative = getInterest(currentTime).negative();
        return new BalanceWithInterest(balance, interestAsNegative);
    }

    public DClient setBalance(long balance, Instant date) {
        this.balance.setBalance(balance, date);
        return this;
    }

    @NotNull
    public Emeralds getInterest(Instant currentTime) {
        Instant lastUpdated = this.balance.getLastUpdated();
        if (lastUpdated.isAfter(currentTime)) {
            String error = "Client{%s}'s balance was last updated at %s, which is later than the current timestamp of %s"
                .formatted(this.getEffectiveName(), lastUpdated, currentTime);
            throw new IllegalArgumentException(error);
        }
        BigDecimal balanceAtStart = this.balance.getAmount().toBigDecimal();
        BigDecimal totalInterest = BigDecimal.ZERO;
        for (DLoan loan : getLoans()) {
            Duration loanDuration = loan.getDuration(lastUpdated, currentTime);
            if (!loanDuration.isPositive()) continue; // consider 0 as well
            Emeralds interest = loan.getInterest(balanceAtStart, lastUpdated, currentTime, false);
            totalInterest = totalInterest.add(interest.toBigDecimal());
        }
        return Emeralds.of(totalInterest);
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

    public void addLoan(DLoan loan) {
        this.loans.add(loan);
    }
}
