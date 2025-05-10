package com.ambrosia.loans.database.account.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.DClientLoanSnapshot;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.HasDateRange;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.LoanApi.LoanQueryApi;
import com.ambrosia.loans.database.account.loan.interest.base.DLoanInterest;
import com.ambrosia.loans.database.account.loan.interest.base.InterestCheckpoint;
import com.ambrosia.loans.database.account.loan.interest.legacy.DLegacyInterestCheckpoint;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.actor.UserActor;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.comment.Commentable;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.database.system.exception.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.database.version.DApiVersion;
import com.ambrosia.loans.database.version.VersionEntityType;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.message.loan.LoanMessageBuilder;
import com.ambrosia.loans.discord.system.log.DiscordLog;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.service.loan.LoanFreezeService;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.History;
import io.ebean.annotation.Identity;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@History
@Entity
@Table(name = "loan")
public class DLoan extends Model implements IAccountChange, LoanAccess, HasDateRange, Commentable {

    @Id
    @Identity(start = 100)
    protected long id;
    @ManyToOne
    protected DClient client;
    @OneToMany(cascade = CascadeType.ALL)
    protected List<DLoanSection> sections;
    @OneToMany(cascade = CascadeType.ALL)
    protected List<DLoanPayment> payments;
    @OneToMany(cascade = CascadeType.ALL)
    protected List<DAdjustLoan> adjustments;
    @OneToMany(cascade = CascadeType.ALL)
    protected List<DCollateral> collateral;
    @OneToMany
    protected List<DClientLoanSnapshot> clientSnapshots = new ArrayList<>();

    @Column
    protected long initialAmount; // positive
    @DbJson
    protected DLoanInterest<?> interest;

    @Index
    @Column(nullable = false)
    protected Timestamp startDate;
    @Index
    @Column
    protected Timestamp endDate;
    @Column(nullable = false)
    protected DLoanStatus status;
    @ManyToOne(optional = false)
    protected DStaffConductor conductor;
    @Embedded(prefix = "")
    protected DLoanMeta meta;
    @OneToMany
    protected List<DComment> comments;

    @ManyToOne
    protected DApiVersion version = DApiVersion.current(VersionEntityType.LOAN);

    public DLoan(DClient client, long initialAmount, double rate, DStaffConductor conductor, Instant startDate) {
        this.client = client;
        this.initialAmount = initialAmount;
        this.conductor = conductor;
        this.startDate = Timestamp.from(startDate);
        this.sections = List.of(new DLoanSection(this, rate, startDate));
        this.status = DLoanStatus.ACTIVE;
        this.meta = new DLoanMeta();
    }

    public DLoan(LoanBuilder request) throws CreateEntityException, InvalidStaffConductorException {
        if (request.getLoanId() != null)
            this.id = request.getLoanId();
        this.client = request.getClient();
        this.initialAmount = request.getAmount().amount();
        this.conductor = request.getConductor();
        this.meta = new DLoanMeta(request);
        this.startDate = Timestamp.from(request.getStartDateOrNow());
        Double rate = request.getRate();
        if (rate == null) throw new CreateEntityException("Rate has not been set!");
        DLoanSection firstSection = new DLoanSection(this, rate, this.startDate.toInstant());
        this.sections = List.of(firstSection);
        this.status = DLoanStatus.ACTIVE;
        this.checkIsFrozen(false);
    }

    public static boolean isWithinPaidBounds(long loanBalance) {
        return Math.abs(loanBalance) < Emeralds.BLOCK;
    }

    @NotNull
    public InterestCheckpoint getLastCheckpoint() {
        @Nullable DClientLoanSnapshot snapshot = LoanQueryApi.findLastLoanSnapshot(this.getId());
        if (snapshot != null) return snapshot.toCheckpoint();
        return createInitialCheckpoint();
    }

    @NotNull
    public InterestCheckpoint createInitialCheckpoint() {
        // todo replace with LegacyInterest when it's implemented
        ApiVersionListLoan version = this.version.getLoan();
        if (version.equals(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY))
            return new DLegacyInterestCheckpoint(this);
        return interest.createInitialCheckpoint(this);
    }

    public DStaffConductor getConductor() {
        return conductor;
    }

    @Override
    public DLoan getEntity() {
        return this;
    }

    @NotNull
    @Override
    public Instant getStartDate() {
        return startDate.toInstant();
    }

    public void setStartDate(Instant newStartDate) {
        Instant oldStartDate = getStartDate();
        if (oldStartDate.equals(newStartDate)) return; // no change in date

        try (Transaction transaction = DB.beginTransaction()) {
            if (oldStartDate.isAfter(newStartDate))
                extendStartDate(newStartDate, transaction);
            else
                truncateStartDate(newStartDate, transaction);

            this.startDate = Timestamp.from(newStartDate);
            this.save(transaction);
            transaction.commit();
        }
    }

    @Nullable
    @Override
    public Instant getEndDate() {
        if (this.endDate == null) return null;
        return this.endDate.toInstant();
    }

    private void extendStartDate(Instant newStartDate, Transaction transaction) {
        DLoanSection section = getSections().get(0);
        section.setStartDate(newStartDate);
        section.save(transaction);
    }

    private void truncateStartDate(Instant newStartDate, Transaction transaction) {
        List<DLoanSection> newSections = new ArrayList<>(getSections());
        for (DLoanSection section : getSections()) {
            if (section.getStartDate().isAfter(newStartDate)) {
                section.delete(transaction); // softDelete
                newSections.remove(0);
            } else if (!section.isEndBefore(newStartDate)) {
                section.setStartDate(newStartDate);
                section.save(transaction);
                break; // the rest of the sections are fine
            }
        }
        this.sections = newSections;
    }

    public List<DLoanSection> getSections() {
        return this.sections.stream()
            .sorted(Comparator.comparing(DLoanSection::getStartDate))
            .toList();
    }

    public void setSections(List<DLoanSection> sections) {
        this.sections = sections;
    }

    public List<DAdjustLoan> getAdjustments() {
        return adjustments.stream()
            .sorted(Comparator.comparing(DAdjustLoan::getDate))
            .toList();
    }

    public Emeralds getTotalOwed() {
        return getInterest(getLastCheckpoint(), Instant.now()).balanceEmeralds();
    }

    public Emeralds getTotalOwed(Instant endDate) {
        return getInterest(getLastCheckpoint(), endDate).balanceEmeralds();
    }

    public InterestCheckpoint getInterest(@Nullable InterestCheckpoint checkpoint, @NotNull Instant end) {
        if (checkpoint == null) // todo maybe can be getLastCheckpoint()
            checkpoint = this.createInitialCheckpoint();
        else checkpoint = checkpoint.copy();

        ApiVersionListLoan version = this.getVersion().getLoan();
        if (version.equals(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY)) {
            checkpoint.resetInterest();
            return getSimpleInterest(end);
        }
        if (interest == null) {
            String msg = "DLoan.interest {%d} is null, but the version is a newer format".formatted(id);
            throw new IllegalStateException(msg);
        }
        return interest.getInterest(checkpoint, end);
    }

    private InterestCheckpoint getSimpleInterest(Instant end) {
        DLegacyInterestCheckpoint checkpoint = new DLegacyInterestCheckpoint(this);
        Instant start = checkpoint.lastUpdated();

        for (DLoanSection section : getSections()) {
            Instant estimatedCheckpoint = section.getEarliestOfEnd(end);
            Duration duration = Bank.legacySimpleWeeksDuration(Duration.between(checkpoint.lastUpdated(), estimatedCheckpoint));
            Instant nextCheckpoint = checkpoint.lastUpdated().plus(duration);
            section.accumulateInterest(checkpoint, nextCheckpoint);
        }

        List<DLoanPayment> payments = getPayments().stream()
            .filter(p -> !p.getDate().isBefore(start))
            .filter(p -> !p.getDate().isAfter(end))
            .toList();
        List<DAdjustLoan> adjustments = this.getAdjustments().stream()
            .filter(a -> !a.getDate().isBefore(start))
            .filter(a -> !a.getDate().isAfter(end))
            .sorted(Comparator.comparing(DAdjustLoan::getDate))
            .toList();

        long paymentsDelta = payments.stream()
            .mapToLong(payment -> payment.getAmount().amount())
            .sum();
        long adjustmentsDelta = adjustments.stream()
            .mapToLong(adjustment -> adjustment.getAmount().amount())
            .sum();

        long totalDelta = -paymentsDelta - adjustmentsDelta;
        checkpoint.updateBalance(totalDelta, end);
        return checkpoint;
    }

    public void makePayment(DLoanPayment payment, Transaction transaction) {
        this.payments.add(payment);
        long loanBalance = getTotalOwed(payment.getDate()).amount();
        if (isWithinPaidBounds(loanBalance)) {
            markPaid(payment.getDate(), transaction);
        }
        payment.save(transaction);
        this.save(transaction);
    }

    public void makeAdjustment(DAdjustLoan adjustment, Transaction transaction) {
        this.adjustments.add(adjustment);
        long loanBalance = getTotalOwed(adjustment.getDate()).amount();
        if (isWithinPaidBounds(loanBalance)) {
            markPaid(adjustment.getDate(), transaction);
        }
        adjustment.save(transaction);
        this.save(transaction);
    }

    public DLoan markPaid(Instant endDate, Transaction transaction) {
        this.status = DLoanStatus.PAID;
        this.endDate = Timestamp.from(endDate);
        getLastSection().setEndDate(endDate).save(transaction);
        getMeta().clearUnfreeze(); // in case it was frozen
        this.save(transaction);
        return this;
    }

    public DLoanSection getLastSection() {
        List<DLoanSection> sections = getSections();
        return sections.get(sections.size() - 1);
    }

    @Override
    public long getId() {
        return this.id;
    }

    public DClient getClient() {
        return this.client;
    }

    @Override
    public Instant getDate() {
        return this.getStartDate();
    }

    @Override
    public void updateSimulation() {
        this.client.updateBalance(this, -this.initialAmount, getDate(), getEventType());
    }

    @Override
    public AccountEventType getEventType() {
        return AccountEventType.LOAN;
    }

    public List<DLoanPayment> getPayments() {
        return this.payments.stream()
            .sorted(Comparator.comparing(DLoanPayment::getDate))
            .toList();
    }

    public DLoanStatus getStatus() {
        return this.status;
    }

    public List<DCollateral> getCollateral() {
        return collateral;
    }

    public Emeralds getInitialAmount() {
        return Emeralds.of(this.initialAmount);
    }

    public DLoan setInitialAmount(Emeralds amount) {
        this.initialAmount = amount.amount();
        return this;
    }

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }

    public void setInterestMeta(DLoanInterest<?> interest) {
        this.interest = interest;
    }

    @NotNull
    public DLoanMeta getMeta() {
        if (this.meta == null) throw new IllegalStateException("loan.meta is null");
        return this.meta;
    }

    public DLoan verifyMeta() {
        if (this.meta == null) this.meta = new DLoanMeta();
        this.meta.verifyInitialized();
        return this;
    }

    public DApiVersion getVersion() {
        return this.version;
    }

    public DLoan setVersion(DApiVersion version) {
        this.version = version;
        return this;
    }

    public boolean checkIsPaid(Instant endDate, Transaction transaction) {
        Emeralds totalOwed = getTotalOwed(endDate);
        if (isWithinPaidBounds(totalOwed.amount())) {
            markPaid(endDate, transaction);
            return true;
        }
        return false;
    }

    public DLoan setDefaulted(@Nullable Instant endDate, boolean defaulted) {
        if (defaulted) {
            Instant endDateInstant = Objects.requireNonNullElseGet(endDate, Instant::now);
            this.status = DLoanStatus.DEFAULTED;
            this.endDate = Timestamp.from(endDateInstant);
        } else {
            this.status = DLoanStatus.ACTIVE;
            this.endDate = null;
        }
        return this;
    }

    public void checkIsFrozen(boolean saveIfChanged) {
        if (this.status.isActive()) {
            if (this.isFrozen()) this.status = DLoanStatus.FROZEN;
            else if (status != DLoanStatus.ACTIVE) this.status = DLoanStatus.ACTIVE;
            else return;
            if (saveIfChanged) this.save();
        }
    }

    public void freeze(Instant effectiveDate, Instant unfreezeDate, double unfreezeToRate, double current, Transaction transaction) {
        changeToNewRate(current, effectiveDate, transaction);
        getMeta().setToUnfreeze(unfreezeDate, unfreezeToRate);
        this.checkIsFrozen(false);
        this.save(transaction);
        LoanFreezeService.refresh();
    }

    public void deletePastFreeze(Instant effectiveDate, double unfrozenRate, Instant pastUnfreezeDate, Double pastUnfreezeRate,
        Transaction transaction) {
        DLoanSection section = getSectionAt(effectiveDate);
        if (section == null) {
            String msg = "There is no section during %s for loan{%d}".formatted(formatDate(effectiveDate), getId());
            throw new IllegalStateException(msg);
        }
        getMeta().setToUnfreeze(pastUnfreezeDate, pastUnfreezeRate);
        section.setRate(unfrozenRate);
        section.save(transaction);
        this.checkIsFrozen(false);
        this.save(transaction);
        LoanFreezeService.refresh();
    }


    public void unfreezeLoan(double unfreezeToRate, @NotNull Instant unfreezeDate) {
        try (Transaction transaction = DB.beginTransaction()) {
            unfreezeLoan(unfreezeToRate, unfreezeDate, transaction);
            transaction.commit();
        }
    }

    public void unfreezeLoan(double unfreezeToRate, @NotNull Instant unfreezeDate, Transaction transaction) {
        DLoanMeta meta = this.getMeta();
        meta.clearUnfreeze();
        this.changeToNewRate(unfreezeToRate, unfreezeDate, transaction);
        this.checkIsFrozen(false);
        this.save(transaction);

        EmbedBuilder embed = new EmbedBuilder().setColor(AmbrosiaColor.BLUE_NORMAL);
        LoanMessageBuilder msgBuilder = LoanMessage.of(this);
        msgBuilder.clientMsg().clientAuthor(embed);
        msgBuilder.loanDescription(embed);

        MessageCreateData message = MessageCreateData.fromEmbeds(embed.build());

        Ambrosia.get().logger().info("Sending unfreeze loan message");
        DiscordLog.unfreezeLoan(this, UserActor.of(DStaffConductor.SYSTEM));
        ClientDiscordDetails discord = this.getClient().getDiscord();
        if (discord == null) {
            String msg = "Cannot attempt to send DM to unfreeze loan for @%s\nClientDiscordDetails is null.".formatted(
                client.getEffectiveName());
            DiscordLog.errorSystem(msg);
            throw new IllegalStateException(msg);
        }
        discord.sendDm(message);
    }
}
