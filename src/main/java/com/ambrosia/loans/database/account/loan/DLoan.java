package com.ambrosia.loans.database.account.loan;

import static com.ambrosia.loans.discord.system.theme.AmbrosiaMessages.formatDate;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.database.version.DApiVersion;
import com.ambrosia.loans.database.version.VersionEntityType;
import com.ambrosia.loans.discord.message.loan.LoanMessage;
import com.ambrosia.loans.discord.message.loan.LoanMessageBuilder;
import com.ambrosia.loans.discord.system.theme.AmbrosiaColor;
import com.ambrosia.loans.service.loan.LoanFreezeService;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
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
import java.util.Objects;
import java.util.function.Predicate;
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
        if (request.getStartDate() == null)
            this.startDate = Timestamp.from(Instant.now());
        else
            this.startDate = Timestamp.from(request.getStartDate());
        Double rate = request.getRate();
        if (rate == null) throw new CreateEntityException("Rate has not been set!");
        DLoanSection firstSection = new DLoanSection(this, rate, this.startDate.toInstant());
        this.sections = List.of(firstSection);
        this.status = DLoanStatus.ACTIVE;
        this.checkIsFrozen(false);
    }


    public static boolean isWithinPaidBounds(long loanBalance) {
        return loanBalance < Emeralds.BLOCK;
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
        return getTotalOwed(getStartDate(), Instant.now());
    }

    public Emeralds getTotalOwed(@Nullable Instant start, Instant endDate) {
        Emeralds owedAtStart = start == null ? getInitialAmount() : getTotalOwed(null, start);
        BigDecimal accountBalanceAtStart = owedAtStart.negative().toBigDecimal();

        Instant startDate = Objects.requireNonNullElseGet(start, this::getStartDate);
        Emeralds interest = getInterest(accountBalanceAtStart, startDate, endDate);
        Emeralds adjustment = getTotalOwedAdjustments(startDate, endDate);
        return interest.add(owedAtStart)
            .add(getTotalPaid(startDate, endDate).negative())
            .add(adjustment.negative());
    }

    private Emeralds getTotalOwedAdjustments(Instant startDate, Instant endDate) {
        Predicate<DAdjustLoan> isDateBetween = ad -> {
            Instant date = ad.getDate();
            return date.isAfter(startDate) ||
                !date.isAfter(endDate);
        };
        return this.adjustments.stream()
            .filter(isDateBetween)
            .map(DAdjustLoan::getAmount)
            .reduce(Emeralds.zero(), Emeralds::add);
    }


    public Emeralds getInterest(BigDecimal accountBalanceAtStart, Instant start, Instant end) {
        DApiVersion version = this.getVersion();
        if (version.getLoan().equals(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY)) {
            return getSimpleInterest(accountBalanceAtStart, start, end);
        }
        return getExactInterest(accountBalanceAtStart, start, end);
    }

    private Emeralds getSimpleInterest(BigDecimal accountBalanceAtStart, Instant assumedStart, Instant end) {
        // todo deduplicate
        BigDecimal runningBalance = accountBalanceAtStart.negate();
        BigDecimal totalInterest = BigDecimal.ZERO;
        if (accountBalanceAtStart.compareTo(BigDecimal.ZERO) > 0) {
            Emeralds bal = Emeralds.of(accountBalanceAtStart);
            String msg = "%s{%d}'s balance of %s is > 0, but has active loan!"
                .formatted(client.getEffectiveName(), client.getId(), bal);
            DatabaseModule.get().logger().warn(msg);
            return Emeralds.zero();
        }

        Duration durationCompleted = Bank.legacySimpleWeeksDuration(Duration.between(getStartDate(), assumedStart));
        Instant actualStart = getStartDate().plus(durationCompleted);

        int paymentIndex = 0;
        int sectionIndex = 0;
        List<DLoanSection> sections = getSections().stream()
            .filter(s -> !s.getStartDate().isBefore(actualStart))
            .filter(s -> !s.getEndDate(end).isBefore(actualStart))
            .toList();
        List<DLoanPayment> payments = getPayments().stream()
            .filter(p -> p.getDate().isAfter(actualStart))
            .filter(p -> p.getDate().isBefore(end))
            .toList();
        Instant checkpoint = actualStart;
        BigDecimal principal = this.getInitialAmount().toBigDecimal();

        // while there's payments, make payments until there's none left
        // each iteration, increment either sectionIndex or paymentIndex
        while (paymentIndex < payments.size()) {
            if (sectionIndex >= sections.size()) break; // payments in future means running simulation
            DLoanSection section = sections.get(sectionIndex);
            DLoanPayment payment = payments.get(paymentIndex);

            Instant sectionEndDate = section.getEndDateOrNow();
            boolean isPaymentEarlierOrEq = !payment.getDate().isAfter(sectionEndDate);
            if (isPaymentEarlierOrEq) {
                // make payment
                Duration duration = Bank.legacySimpleWeeksDuration(Duration.between(getStartDate(), checkpoint));
                Instant actualCheckpoint = getStartDate().plus(duration);
                BigDecimal sectionInterest = section.getInterest(actualCheckpoint, payment.getDate(), principal);
                BigDecimal paymentAmount = payment.getAmount().toBigDecimal();
                runningBalance = runningBalance.add(sectionInterest).subtract(paymentAmount);
                totalInterest = totalInterest.add(sectionInterest);
                checkpoint = payment.getDate();
                paymentIndex++;
            } else {
                Duration duration = Bank.legacySimpleWeeksDuration(Duration.between(getStartDate(), checkpoint));
                Instant actualCheckpoint = getStartDate().plus(duration);
                BigDecimal sectionInterest = section.getInterest(actualCheckpoint, end, principal);
                checkpoint = section.getEndDate(end);
                runningBalance = runningBalance.add(sectionInterest);
                totalInterest = totalInterest.add(sectionInterest);
                sectionIndex++;
            }
        }
        while (sectionIndex < sections.size()) {
            DLoanSection section = sections.get(sectionIndex);
            Duration duration = Bank.legacySimpleWeeksDuration(Duration.between(getStartDate(), checkpoint));
            Instant actualCheckpoint = getStartDate().plus(duration);
            BigDecimal sectionInterest = section.getInterest(actualCheckpoint, end, principal);
            checkpoint = section.getEndDate(end);
            totalInterest = totalInterest.add(sectionInterest);
            sectionIndex++;
        }
        return Emeralds.of(totalInterest);
    }


    private Emeralds getExactInterest(BigDecimal accountBalanceAtStart, Instant start, Instant end) {
        BigDecimal runningBalance = accountBalanceAtStart.negate();
        BigDecimal totalInterest = BigDecimal.ZERO;
        if (accountBalanceAtStart.compareTo(BigDecimal.ZERO) > 0) {
            Emeralds bal = Emeralds.of(accountBalanceAtStart);
            String msg = "%s{%d}'s balance of %s is > 0, but has active loan!"
                .formatted(client.getEffectiveName(), client.getId(), bal);
            DatabaseModule.get().logger().warn(msg);
            return Emeralds.zero();
        }

        int paymentIndex = 0;
        int sectionIndex = 0;
        List<DLoanSection> sections = getSections().stream()
            .filter(s -> s.getEndDate(end).isAfter(start))
            .toList();
        List<DLoanPayment> payments = getPayments().stream()
            .filter(p -> p.getDate().isAfter(start))
            .toList();
        BigDecimal initialLoanBal = this.getInitialAmount().toBigDecimal();
        Instant checkpoint = start;
        BigDecimal principal = initialLoanBal;

        // while there's payments, make payments until there's none left
        // each iteration, increment either sectionIndex or paymentIndex
        while (paymentIndex < payments.size()) {
            if (sectionIndex >= sections.size()) break; // payments in future means running simulation
            DLoanSection section = sections.get(sectionIndex);
            DLoanPayment payment = payments.get(paymentIndex);

            Instant sectionEndDate = section.getEndDateOrNow();
            boolean isPaymentEarlierOrEq = !payment.getDate().isAfter(sectionEndDate);
            principal = principal.min(runningBalance);
            if (isPaymentEarlierOrEq) {
                if (!payment.getDate().isBefore(end)) break;
                // make payment
                BigDecimal sectionInterest = section.getInterest(checkpoint, payment.getDate(), principal);
                BigDecimal paymentAmount = payment.getAmount().toBigDecimal();
                runningBalance = runningBalance.add(sectionInterest).subtract(paymentAmount);
                totalInterest = totalInterest.add(sectionInterest);
                checkpoint = payment.getDate();
                paymentIndex++;
            } else {
                BigDecimal sectionInterest = section.getInterest(checkpoint, end, principal);
                checkpoint = section.getEndDate(end);
                runningBalance = runningBalance.add(sectionInterest);
                totalInterest = totalInterest.add(sectionInterest);
                sectionIndex++;
            }
        }
        principal = principal.min(runningBalance);
        while (sectionIndex < sections.size()) {
            DLoanSection section = sections.get(sectionIndex);
            BigDecimal sectionInterest = section.getInterest(checkpoint, end, principal);
            checkpoint = section.getEndDate(end);
            totalInterest = totalInterest.add(sectionInterest);
            sectionIndex++;
        }
        return Emeralds.of(totalInterest);
    }

    public DClient getClient() {
        return this.client;
    }

    public void makePayment(DLoanPayment payment, Transaction transaction) {
        this.payments.add(payment);
        if (isWithinPaidBounds(getTotalOwed(null, payment.getDate()).amount())) {
            markPaid(payment.getDate(), transaction);
        }
        payment.save(transaction);
        this.save(transaction);
    }

    public DLoan markPaid(Instant endDate, Transaction transaction) {
        this.status = DLoanStatus.PAID;
        this.endDate = Timestamp.from(endDate);
        getLastSection().setEndDate(endDate).save(transaction);
        this.meta.clearUnfreeze(); // in case it was frozen
        this.save(transaction);
        return this;
    }

    public DLoanSection getLastSection() {
        List<DLoanSection> sections = getSections();
        return sections.get(sections.size() - 1);
    }

    public long getId() {
        return this.id;
    }

    public List<DLoanPayment> getPayments() {
        return this.payments.stream()
            .sorted(Comparator.comparing(DLoanPayment::getDate))
            .toList();
    }

    @Override
    public Instant getDate() {
        return this.getStartDate();
    }

    @Override
    public void updateSimulation() {
        this.client.updateBalance(-this.initialAmount, getDate(), getEventType());
    }

    @Override
    public AccountEventType getEventType() {
        return AccountEventType.LOAN;
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
        return this.meta;
    }

    public DApiVersion getVersion() {
        return this.version;
    }

    public DLoan setVersion(DApiVersion version) {
        this.version = version;
        return this;
    }

    public void checkIsPaid(Instant endDate, Transaction transaction) {
        Emeralds totalOwed = getTotalOwed(null, endDate);
        if (isWithinPaidBounds(totalOwed.amount())) {
            markPaid(endDate, transaction);
        }
    }

    public DLoan setDefaulted(Instant endDate, boolean defaulted) {
        if (defaulted) {
            this.status = DLoanStatus.DEFAULTED;
            Instant endDateInstant = Objects.requireNonNullElseGet(endDate, Instant::now);
            this.endDate = Timestamp.from(endDateInstant);
        } else {
            this.status = DLoanStatus.ACTIVE;
            this.endDate = null;
        }
        return this;
    }

    public void checkIsFrozen(boolean saveIfChanged) {
        if (this.status.isActive()) {
            if (this.isFrozen() && this.status != DLoanStatus.FROZEN) this.status = DLoanStatus.FROZEN;
            else if (status != DLoanStatus.ACTIVE) this.status = DLoanStatus.ACTIVE;
            else return;
            if (saveIfChanged) this.save();
        }
    }

    public void freeze(Instant effectiveDate, Instant unfreezeDate, double unfreezeRate, double current, Transaction transaction) {
        changeToNewRate(current, effectiveDate, transaction);
        this.meta.setToUnfreeze(unfreezeDate, unfreezeRate);
        this.checkIsFrozen(false);
        this.save(transaction);
        LoanFreezeService.refresh();
    }

    public void deletePastFreeze(Instant effectiveDate, double unfrozenRate, Transaction transaction) {
        DLoanSection section = getSectionAt(effectiveDate);
        if (section == null) {
            String msg = "There is no section during %s for loan{%d}".formatted(formatDate(effectiveDate), getId());
            throw new IllegalStateException(msg);
        }
        this.meta.clearUnfreeze();
        section.setRate(unfrozenRate);
        this.checkIsFrozen(false);
        section.save(transaction);
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

        // todo could be async
        EmbedBuilder embed = new EmbedBuilder().setColor(AmbrosiaColor.BLUE_NORMAL);
        LoanMessageBuilder msgBuilder = LoanMessage.of(this);
        msgBuilder.clientMsg().clientAuthor(embed);
        msgBuilder.loanDescription(embed);

        MessageCreateData message = MessageCreateData.fromEmbeds(embed.build());
        // todo log channel to inform staff
        //      also record messages in db
        Ambrosia.get().logger().info("Sent unfreeze loan message");
        this.getClient().getDiscord().sendDm(message);
    }
}
