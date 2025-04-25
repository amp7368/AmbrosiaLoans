package com.ambrosia.loans.database.account.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.DClientLoanSnapshot;
import com.ambrosia.loans.database.account.DClientSnapshot;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
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

    @OneToMany
    protected List<DClientLoanSnapshot> clientSnapshots = new ArrayList<>();
    @Id
    @Identity(start = 100)
    private long id;
    @ManyToOne
    private DClient client;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DLoanSection> sections;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DLoanPayment> payments;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DAdjustLoan> adjustments;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DCollateral> collateral;
    @Column
    private long initialAmount; // positive
    @Index
    @Column(nullable = false)
    private Timestamp startDate;
    @Index
    @Column
    private Timestamp endDate;
    @Column(nullable = false)
    private DLoanStatus status;
    @ManyToOne(optional = false)
    private DStaffConductor conductor;
    @Embedded(prefix = "")
    private DLoanMeta meta;
    @OneToMany
    private List<DComment> comments;
    @ManyToOne
    private DApiVersion version = DApiVersion.current(VersionEntityType.LOAN);

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

    public InterestCheckpoint getLastCheckpoint() {
        return clientSnapshots.stream()
            .max(DClientSnapshot.COMPARATOR)
            .map(InterestCheckpoint::new)
            .orElseGet(() -> new InterestCheckpoint(this));
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

    public Emeralds getTotalOwed() {
        return getTotalOwed(Instant.now());
    }

    public Emeralds getTotalOwed(Instant endDate) {
        InterestCheckpoint checkpoint = new InterestCheckpoint(this);
        return getInterest(checkpoint, endDate).balanceEmeralds();
    }

    public InterestCheckpoint getInterest(@Nullable InterestCheckpoint checkpoint, @NotNull Instant end) {
        if (checkpoint == null) checkpoint = new InterestCheckpoint(this);

        DApiVersion version = this.getVersion();
        if (version.getLoan().equals(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY)) {
            return getSimpleInterest(end);
        }
        return getExactInterest(checkpoint, end);
    }

    private InterestCheckpoint getSimpleInterest(Instant end) {
        InterestCheckpoint checkpoint = new InterestCheckpoint(this);
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
        List<DAdjustLoan> adjustments = this.adjustments.stream()
            .filter(a -> !a.getDate().isBefore(start))
            .filter(a -> !a.getDate().isAfter(end))
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

    private InterestCheckpoint getExactInterest(InterestCheckpoint checkpoint, @NotNull Instant end) {
        checkpoint.resetInterest();

        if (checkpoint.balance() < 0) {
            Emeralds bal = Emeralds.of(checkpoint.balance());
            String msg = "%s{%d}'s balance of %s is > 0, but has active loan!"
                .formatted(client.getEffectiveName(), client.getId(), bal);
            DatabaseModule.get().logger().warn(msg);
            return checkpoint;
        }

        int sectionIndex = 0;
        int paymentIndex = 0;
        int adjustmentIndex = 0;
        List<DLoanSection> sections = getSections().stream()
            .filter(s -> s.getStartDate().isBefore(end))
            .filter(s -> s.getEarliestOfEnd(end).isAfter(checkpoint.lastUpdated()))
            .toList();
        List<DLoanPayment> payments = getPayments().stream()
            .filter(p -> p.getDate().isAfter(checkpoint.lastUpdated()))
            .toList();
        List<DAdjustLoan> adjustments = this.adjustments.stream()
            .filter(a -> a.getDate().isAfter(checkpoint.lastUpdated()))
            .toList();

        // while there's payments, make payments until there's none left
        // each iteration, increment either sectionIndex or paymentIndex
        while (paymentIndex < payments.size() || adjustmentIndex < adjustments.size()) {
            if (sectionIndex >= sections.size()) break; // payments in future means running simulation

            boolean hasPayment = paymentIndex < payments.size();
            boolean hasAdjustment = adjustmentIndex < adjustments.size();
            DLoanPayment payment = hasPayment ? payments.get(paymentIndex) : null;
            DAdjustLoan adjustment = hasAdjustment ? adjustments.get(adjustmentIndex) : null;

            DLoanSection section = sections.get(sectionIndex);
            Instant sectionEndDate = section.getEarliestOfEnd(end);

            boolean isSectionLater = false;
            if (hasPayment)
                isSectionLater = !payment.getDate().isAfter(sectionEndDate);
            if (hasAdjustment && !isSectionLater)
                isSectionLater = !adjustment.getDate().isAfter(sectionEndDate);
            if (hasPayment && hasAdjustment) {
                hasPayment = payment.getDate().isBefore(adjustment.getDate());
            }
            if (isSectionLater) {
                if (hasPayment) paymentIndex++;
                else adjustmentIndex++;

                Emeralds delta = hasPayment ? payment.getAmount().negative() : adjustment.getAmount().negative();
                Instant date = hasPayment ? payment.getDate() : adjustment.getDate();
                boolean isDateAfterOrEq = date.isAfter(end);
                if (isDateAfterOrEq) break;
                // make balance change
                section.accumulateInterest(checkpoint, date);
                checkpoint.updateBalance(delta.amount(), date);
            } else {
                Instant date = section.getEarliestOfEnd(end);
                section.accumulateInterest(checkpoint, date);
                sectionIndex++;
            }
        }
        while (sectionIndex < sections.size()) {
            DLoanSection section = sections.get(sectionIndex);
            Instant date = section.getEarliestOfEnd(end);
            section.accumulateInterest(checkpoint, date);
            sectionIndex++;
        }
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

    public DLoanMeta getMeta() {
        if (this.meta == null) this.meta = new DLoanMeta(); // todo why is this needed?
        return this.meta;
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
