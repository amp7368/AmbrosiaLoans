package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.event.base.AccountEvent;
import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.balance.ClientBalance;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.migrate.client.ImportedClient;
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
public class DClient extends Model implements ClientAccess, Commentable {

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
    @Column(unique = true)
    private String displayName;
    @Column(nullable = false)
    private Timestamp dateCreated = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private boolean blacklisted = false;

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();

    @Column
    @Embedded(prefix = "balance_")
    private final ClientBalance balance = new ClientBalance();
    @OneToMany
    private final List<DAccountSnapshot> accountSnapshots = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    private final List<DLoan> loans = new ArrayList<>();
    @OneToMany
    private final List<DInvestment> investments = new ArrayList<>();
    @OneToMany
    private final List<DWithdrawal> withdrawals = new ArrayList<>();
    @OneToMany
    private final List<DAdjustBalance> adjustments = new ArrayList<>();

    public DClient(String displayName) {
        this.displayName = displayName;
    }

    public DClient(ImportedClient imported) {
        this.id = imported.getId();
        this.minecraft = imported.getMinecraft();
        this.discord = imported.getDiscord();
        this.dateCreated = imported.getDateCreated();
    }

    public long getId() {
        return id;
    }

    public Emeralds getBalance(Instant currentTime) throws IllegalArgumentException {
        return getBalanceWithRecentInterest(currentTime)
            .totalEmeralds();
    }

    public Emeralds getInvestBalance(Instant now) {
        return getBalanceWithRecentInterest(now)
            .investBalance();
    }

    public BalanceWithInterest getBalanceWithRecentInterest(Instant currentTime) throws IllegalArgumentException {
        Emeralds investAmount = this.balance.getInvestAmount();
        Emeralds loanAmount = this.balance.getLoanAmount();
        Emeralds interestAsNegative = getInterest(currentTime).negative();
        return new BalanceWithInterest(investAmount, loanAmount, interestAsNegative);
    }

    DClient setBalance(long investAmount, long loanAmount, Instant date) {
        this.balance.setBalance(investAmount, loanAmount, date);
        return this;
    }

    @NotNull
    private Emeralds getInterest(Instant currentTime) throws IllegalArgumentException {
        this.refresh();
        Instant lastUpdated = this.balance.getLastUpdated();
        if (willBalanceFailAtTimestamp(currentTime)) {
            String error = "Client{%s}'s balance was last updated at %s, which is later than the current timestamp of %s"
                .formatted(this.getEffectiveName(), lastUpdated, currentTime);
            throw new IllegalArgumentException(error);
        }
        BigDecimal totalInterest = BigDecimal.ZERO;
        for (DLoan loan : getLoans()) {
            Duration loanDuration = loan.getDuration(lastUpdated, currentTime);
            if (loanDuration.isNegative()) continue; // todo ??? consider 0 as well

            // if we call this for running a simulation, we don't want to include payments.
            // However,
            BigDecimal balanceAtStart = loan.getTotalOwed(null, lastUpdated).negative().toBigDecimal();
            Emeralds interest = loan.getInterest(balanceAtStart, lastUpdated, currentTime);
            totalInterest = totalInterest.add(interest.toBigDecimal());
        }
        return Emeralds.of(totalInterest);
    }

    public boolean willBalanceFailAtTimestamp(Instant currentTime) {
        Instant lastUpdated = this.balance.getLastUpdated();
        return lastUpdated.isAfter(currentTime);
    }

    @Override
    public boolean isBlacklisted() {
        return blacklisted;
    }

    @Override
    public DClient setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
        return this;
    }

    @Override
    public DClient getEntity() {
        return this;
    }


    public List<DLoan> getLoans() {
        return loans.stream()
            .sorted(Comparator.comparing(DLoan::getStartDate))
            .toList();
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

    public List<DAccountSnapshot> getAccountSnapshots() {
        return accountSnapshots.stream()
            .sorted()
            .toList();
    }

    public ClientDiscordDetails getDiscord() {
        return this.discord;
    }

    public DClient setDiscord(ClientDiscordDetails discord) {
        this.discord = discord;
        return this;
    }

    @Override
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

    public void addWithdrawal(DWithdrawal withdrawal) {
        this.withdrawals.add(withdrawal);
    }

    public void addInvestment(DInvestment investment) {
        this.investments.add(investment);
    }

    public List<AccountEvent> getInvestmentLike() {
        List<AccountEvent> events = new ArrayList<>(this.investments);
        events.addAll(this.withdrawals);
        events.addAll(this.adjustments);
        return events;
    }

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }
}
