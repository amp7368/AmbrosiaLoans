package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.ClientMergedSnapshot;
import com.ambrosia.loans.database.account.DClientInvestSnapshot;
import com.ambrosia.loans.database.account.DClientLoanSnapshot;
import com.ambrosia.loans.database.account.adjust.DAdjustBalance;
import com.ambrosia.loans.database.account.base.AccountEvent;
import com.ambrosia.loans.database.account.investment.DInvestment;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.balance.ClientBalance;
import com.ambrosia.loans.database.entity.client.meta.DClientMeta;
import com.ambrosia.loans.database.entity.client.settings.DClientSettings;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.username.DNameHistory;
import com.ambrosia.loans.database.entity.client.username.NameHistoryType;
import com.ambrosia.loans.database.entity.client.username.query.QDNameHistory;
import com.ambrosia.loans.database.message.comment.Commentable;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.migrate.client.ImportedClient;
import com.ambrosia.loans.service.name.UpdateClientMetaHook;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.annotation.Cache;
import io.ebean.annotation.History;
import io.ebean.annotation.Identity;
import io.ebean.annotation.Index;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Cache(enableQueryCache = true, naturalKey = {"discord.id"})
@History
@Entity
@Table(name = "client")
public class DClient extends Model implements ClientAccess, Commentable {

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();
    @Column
    @Embedded(prefix = "balance_")
    private final ClientBalance balance = new ClientBalance();
    @OneToMany
    private final List<DClientLoanSnapshot> loanSnapshots = new ArrayList<>();
    @OneToMany
    private final List<DClientInvestSnapshot> investSnapshots = new ArrayList<>();
    @OneToMany(mappedBy = "client")
    private final List<DLoan> loans = new ArrayList<>();
    @OneToMany
    private final List<DInvestment> investments = new ArrayList<>();
    @OneToMany
    private final List<DWithdrawal> withdrawals = new ArrayList<>();
    @OneToMany
    private final List<DAdjustBalance> adjustments = new ArrayList<>();
    @OneToMany
    private final List<DNameHistory> nameHistory = new ArrayList<>();

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
    @Index
    @Column(unique = true)
    private String displayName;
    @Column(nullable = false)
    private Timestamp dateCreated = Timestamp.from(Instant.now());
    @Column(nullable = false)
    private boolean blacklisted = false;
    @JoinColumn
    @OneToOne
    private DClientMeta meta;
    @OneToOne(mappedBy = "client")
    private DClientSettings settings;

    public DClient(String displayName) {
        this.displayName = displayName;
    }

    public DClient(String displayName, ClientMinecraftDetails minecraft, ClientDiscordDetails discord) {
        this.displayName = displayName;
        this.minecraft = minecraft;
        this.discord = discord;
    }

    public DClient(ImportedClient imported) {
        this.id = imported.getId();
        this.minecraft = imported.getMinecraft();
        this.discord = imported.getDiscord();
        this.dateCreated = imported.getDateCreated();
    }

    public DClientSettings getSettings() {
        refresh();
        if (settings != null) return settings;
        settings = new DClientSettings(this);
        settings.save();
        save();
        return settings;
    }

    public DClientMeta getMeta() {
        if (this.meta != null) return meta;

        this.meta = new DClientMeta(this);
        try (Transaction transaction = DB.beginTransaction()) {
            meta.save(transaction);
            save(transaction);
            transaction.commit();
        }
        return meta;
    }

    public DNameHistory getNameNow(NameHistoryType nameType) {
        List<DNameHistory> history = getNameHistory(nameType);
        if (!history.isEmpty()) {
            DNameHistory now = history.get(0);
            if (!now.isCurrent()) {
                String msg = "First entry in name history is not currently in use!";
                DiscordLog.error(msg, UserActor.of(this));
            }
            return now;
        }
        DNameHistory firstName = nameType.createFromClient(this);
        firstName.save();
        this.refresh();
        return firstName;
    }

    public DNameHistory getNameAt(Instant date, NameHistoryType nameType) {
        for (DNameHistory name : getNameHistory(nameType)) {
            if (name.isUsedAt(date)) {
                return name;
            }
        }
        DNameHistory firstName = nameType.createFromClient(this);
        firstName.save();
        this.refresh();
        return firstName;
    }

    public List<DNameHistory> getNameHistory() {
        return this.nameHistory;
    }

    public List<DNameHistory> getNameHistory(@Nullable NameHistoryType nameType) {
        QDNameHistory query = new QDNameHistory().where()
            .client.eq(this);
        if (nameType != null) query.type.eq(nameType);

        return query
            .orderBy().firstUsed.desc()
            .findList();
    }

    public long getId() {
        return id;
    }

    public Emeralds getBalance(Instant currentTime) throws IllegalArgumentException {
        return getBalanceWithRecentInterest(currentTime)
            .totalEmeralds();
    }

    public Emeralds getInvestBalanceNow() {
        return this.balance.getInvestAmount();
    }

    public Emeralds getLoanBalance(Instant now) {
        return getBalanceWithRecentInterest(now)
            .loanTotal();
    }

    public BalanceWithInterest getBalanceWithRecentInterest(Instant currentTime) throws IllegalArgumentException {
        Emeralds investAmount = this.balance.getInvestAmount();
        Emeralds loanAmount = this.balance.getLoanAmount();

        List<DLoan> legacyLoans = getLoans().stream()
            .filter(loan -> loan.getVersion().is(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY))
            .filter(loan -> !loan.getEndDate(currentTime).isBefore(currentTime))
            .toList();
        Emeralds legacyInterest;
        if (legacyLoans.isEmpty()) legacyInterest = Emeralds.zero();
        else legacyInterest = legacyLoans.stream()
            .map(loan -> loan.getInterest(null, null, currentTime))
            .reduce(Emeralds.zero(), Emeralds::add)
            .negative();

        Emeralds interestAsNegative = getInterest(currentTime).negative();
        return new BalanceWithInterest(investAmount, loanAmount, interestAsNegative, legacyInterest);
    }

    Emeralds addLoanBalance(long loanDelta, Instant date) {
        return this.balance.addLoanBalance(loanDelta, date);
    }

    Emeralds addInvestBalance(long investDelta, Instant date) {
        return this.balance.addInvestBalance(investDelta, date);
    }

    @NotNull
    private Emeralds getInterest(Instant currentTime) throws IllegalArgumentException {
        this.refresh();
        Instant lastUpdated = this.balance.getLoanLastUpdated();
        if (willBalanceFailAtTimestamp(currentTime)) {
            String error = "Client{%s}'s balance was last updated at %s, which is later than the current timestamp of %s"
                .formatted(this.getEffectiveName(), lastUpdated, currentTime);
            throw new IllegalArgumentException(error);
        }
        BigDecimal totalInterest = BigDecimal.ZERO;
        List<DLoan> loans = getLoans().stream()
            .filter(loan -> !loan.getVersion().is(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY))
            .toList();
        for (DLoan loan : loans) {
            Duration loanDuration = loan.getDuration(lastUpdated, currentTime);
            if (loanDuration.isNegative()) continue;
            if (loanDuration.isZero()) continue;

            // if we call this for running a simulation, we don't want to include payments.
            // However,
            BigDecimal balanceAtStart = loan.getTotalOwed(null, lastUpdated).negative().toBigDecimal();
            Emeralds interest = loan.getInterest(balanceAtStart, lastUpdated, currentTime);
            totalInterest = totalInterest.add(interest.toBigDecimal());
        }
        return Emeralds.of(totalInterest);
    }

    public boolean willBalanceFailAtTimestamp(Instant currentTime) {
        Instant lastUpdated = this.balance.getLoanLastUpdated();
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

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ClientMinecraftDetails getMinecraft() {
        return minecraft.setClient(this);
    }

    public DClient setMinecraft(ClientMinecraftDetails minecraft) {
        this.minecraft.setAll(minecraft);
        return this;
    }

    public ClientDiscordDetails getDiscord(boolean shouldUpdate) {
        if (discord == null) return null;
        discord.setClient(this);
        if (shouldUpdate) UpdateClientMetaHook.hookUpdate(this);
        return this.discord;
    }

    @Nullable
    public ClientDiscordDetails getDiscord() {
        return getDiscord(true);
    }

    public DClient setDiscord(ClientDiscordDetails discord) {
        if (this.discord == null)
            this.discord = discord;
        this.discord.setAll(discord);
        return this;
    }

    public DClient addAccountSnapshot(DClientInvestSnapshot snapshot) {
        this.investSnapshots.add(snapshot);
        return this;
    }

    public DClient addAccountSnapshot(DClientLoanSnapshot snapshot) {
        this.loanSnapshots.add(snapshot);
        return this;
    }

    public List<DClientLoanSnapshot> getLoanSnapshots() {
        return loanSnapshots.stream()
            .sorted()
            .toList();
    }

    // get non-legacy investment snapshots
    public List<DClientInvestSnapshot> getInvestSnapshots() {
        return investSnapshots.stream()
            .filter(Predicate.not(DClientInvestSnapshot::isLegacy))
            .sorted()
            .toList();
    }

    /**
     * @return Gets merged snapshots after migration day
     */
    public List<ClientMergedSnapshot> getMergedSnapshots() {
        List<DClientLoanSnapshot> loanSnapshots = getLoanSnapshots();
        List<DClientInvestSnapshot> investSnapshots = getInvestSnapshots();
        if (loanSnapshots.isEmpty() && investSnapshots.isEmpty()) return List.of();

        int loanI = 0;
        int investI = 0;
        Emeralds lastLoanBalance = Emeralds.zero();
        Emeralds lastInvestBalance = Emeralds.zero();
        DClientLoanSnapshot nextLoan = loanI < loanSnapshots.size() ? loanSnapshots.get(loanI) : null;
        DClientInvestSnapshot nextInvest = investI < investSnapshots.size() ? investSnapshots.get(investI) : null;

        List<ClientMergedSnapshot> mergedSnapshots = new ArrayList<>(loanSnapshots.size() + investSnapshots.size());
        ClientMergedSnapshot merged = null;

        while (nextLoan != null || nextInvest != null) {
            boolean isLoan;
            if (nextLoan != null && nextInvest != null)
                isLoan = nextLoan.getDate().isBefore(nextInvest.getDate());
            else isLoan = nextLoan != null;

            Instant date = isLoan ? nextLoan.getDate() : nextInvest.getDate();
            if (merged == null) {
                merged = new ClientMergedSnapshot(date, lastLoanBalance, lastInvestBalance);
            }

            // if successful merge, move on
            // otherwise, add current to mergedSnapshots and stay on nextLoan|nextInvest
            if (merged.tryMerge(isLoan ? nextLoan : nextInvest)) {
                if (isLoan) {
                    lastLoanBalance = nextLoan.getBalance();
                    loanI++;
                    nextLoan = loanI < loanSnapshots.size() ? loanSnapshots.get(loanI) : null;
                } else {
                    lastInvestBalance = nextInvest.getBalance();
                    investI++;
                    nextInvest = investI < investSnapshots.size() ? investSnapshots.get(investI) : null;
                }
            } else {
                mergedSnapshots.add(merged);
                merged = null;
            }
        }

        if (merged != null) mergedSnapshots.add(merged);
        return mergedSnapshots;
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

    public List<DAdjustBalance> getAdjustments() {
        return adjustments;
    }

    @Override
    public String toString() {
        return getEffectiveName();
    }
}
