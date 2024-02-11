package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.event.adjust.DAdjustLoan;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.base.IAccountChange;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.database.system.CreateEntityException;
import com.ambrosia.loans.database.version.DApiVersion;
import com.ambrosia.loans.database.version.VersionEntityType;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.Identity;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;
    @Column(nullable = false)
    private DLoanStatus status;
    @ManyToOne(optional = false)
    private DStaffConductor conductor;
    @Column
    @DbDefault("none")
    private String reason;
    @Column
    @DbDefault("none")
    private String repayment;
    @OneToOne
    private DClient vouch;
    @Column
    @DbDefault("none")
    private String discount;
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
    }

    public DLoan(LoanBuilder request) throws CreateEntityException, InvalidStaffConductorException {
        if (request.getLoanId() != null)
            this.id = request.getLoanId();
        this.client = request.getClient();
        this.initialAmount = request.getAmount().amount();
        this.conductor = request.getConductor();
        this.reason = request.getReason();
        this.repayment = request.getRepayment();
        this.vouch = request.getVouchClient();
        if (request.getStartDate() == null)
            this.startDate = Timestamp.from(Instant.now());
        else
            this.startDate = Timestamp.from(request.getStartDate());
        Double rate = request.getRate();
        if (rate == null) throw new CreateEntityException("Rate has not been set!");
        DLoanSection firstSection = new DLoanSection(this, rate, this.startDate.toInstant());
        this.sections = List.of(firstSection);
        this.discount = request.getDiscount();
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
        return this.startDate.toInstant();
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

        Emeralds adjustment = getTotalOwedAdjustments(endDate, startDate);
        return interest.add(this.initialAmount)
            .add(getTotalPaid(startDate, endDate).negative())
            .add(adjustment.negative());
    }

    private Emeralds getTotalOwedAdjustments(Instant endDate, Instant startDate) {
        Predicate<DAdjustLoan> isDateBetween = ad -> {
            Instant date = ad.getDate();
            return !date.isBefore(startDate) ||
                !date.isAfter(endDate);
        };
        return this.adjustments.stream()
            .filter(isDateBetween)
            .map(DAdjustLoan::getAmount)
            .reduce(Emeralds.zero(), Emeralds::add);
    }

    public Emeralds getInterest(BigDecimal accountBalanceAtStart, Instant start, Instant end) {
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
            .filter(s -> !s.getEndDate(end).isBefore(start))
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
        if (isWithinPaidBounds(getTotalOwed().amount())) {
            markPaid(payment.getDate(), transaction);
        }
        payment.save(transaction);
        this.save(transaction);
    }

    public DLoan markPaid(Instant endDate, Transaction transaction) {
        this.status = DLoanStatus.PAID;
        this.endDate = Timestamp.from(endDate);
        getLastSection().setEndDate(endDate).save(transaction);
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

    @Override
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

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }

    public void checkIsFrozen(boolean saveIfChanged) {
        if (this.status.isActive()) {
            if (this.isFrozen() && this.status != DLoanStatus.FROZEN) this.status = DLoanStatus.FROZEN;
            else if (status != DLoanStatus.ACTIVE) this.status = DLoanStatus.ACTIVE;
            else return;
            if (saveIfChanged) this.save();
        }
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

    public void setDefaulted() {
        this.status = DLoanStatus.DEFAULTED;
    }
}
