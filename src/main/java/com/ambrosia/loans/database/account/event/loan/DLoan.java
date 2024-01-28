package com.ambrosia.loans.database.account.event.loan;

import com.ambrosia.loans.database.DatabaseModule;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.base.IAccountChange;
import com.ambrosia.loans.database.account.event.loan.collateral.DCollateral;
import com.ambrosia.loans.database.account.event.loan.section.DLoanSection;
import com.ambrosia.loans.database.account.event.payment.DLoanPayment;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.database.util.CreateEntityException;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.loan.ActiveRequestLoan;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Model;
import io.ebean.Transaction;
import io.ebean.annotation.Identity;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    private List<DCollateral> collateral;
    @Column
    private long initialAmount;
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;
    @Column(nullable = false)
    private DLoanStatus status;
    @ManyToOne(optional = false)
    private DStaffConductor conductor;
    @Column
    private String reason;
    @OneToOne
    private DClient vouch;
    @Column
    private String discount;
    @Column
    private String repayment;
    @OneToMany
    private List<DComment> comments;

    public DLoan(DClient client, long initialAmount, double rate, DStaffConductor conductor, Instant startDate) {
        this.client = client;
        this.initialAmount = initialAmount;
        this.conductor = conductor;
        this.startDate = Timestamp.from(startDate);
        this.sections = List.of(new DLoanSection(this, rate, startDate));
        this.status = DLoanStatus.ACTIVE;
    }

    public DLoan(ActiveRequestLoan request) throws CreateEntityException, InvalidStaffConductorException {
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
    }


    @Nullable
    private static BigDecimal calcPayment(DLoanSection section, DLoanPayment payment, BigDecimal amount) {
        Instant sectionEndDate = section.getEndDate();
        Instant sectionEndDateOrNow = Objects.requireNonNullElseGet(sectionEndDate, Instant::now);
        BigDecimal sectionRate = BigDecimal.valueOf(section.getRate());
        Instant paymentDate = payment.getDate();

        boolean isPaymentLater = sectionEndDate != null && paymentDate.isAfter(sectionEndDate);
        if (isPaymentLater) return null;

        Duration duration = Duration.between(paymentDate, sectionEndDateOrNow);
        return payment.getEffectiveAmount(duration, sectionRate);
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

    public Emeralds getTotalOwed() {
        BigDecimal negativeInitialAmount = BigDecimal.valueOf(-initialAmount);
        Emeralds interest = getInterest(negativeInitialAmount, getStartDate(), Instant.now(), true);
        return interest.add(this.initialAmount);
    }

    public Emeralds getInterest(BigDecimal accountBalanceAtStart, Instant start, Instant end, boolean includePayments) {
        BigDecimal balance = accountBalanceAtStart.negate();
        if (accountBalanceAtStart.compareTo(BigDecimal.ZERO) > 0) {
            String msg = "%s{%d}'s balance > 0, but has active loan!".formatted(client.getEffectiveName(), client.getId());
            DatabaseModule.get().logger().warn(msg);
            return Emeralds.of(0);
        }
        int paymentIndex = 0;
        for (DLoanSection section : getSections()) {
            BigDecimal sectionInterest = section.getInterest(start, end, balance);
            balance = balance.add(sectionInterest);

            if (!includePayments) continue;
            while (paymentIndex < payments.size()) {
                DLoanPayment payment = payments.get(paymentIndex);
                BigDecimal paymentAmount = calcPayment(section, payment, balance);
                if (paymentAmount == null) break;
                balance = balance.subtract(paymentAmount);
                paymentIndex++;
            }
        }
        return Emeralds.of(balance.subtract(accountBalanceAtStart.negate()));
    }

    public DClient getClient() {
        return this.client;
    }

    public void makePayment(DLoanPayment payment, Transaction transaction) {
        this.payments.add(payment);
        if (isWithinPaidBounds(getTotalOwed().amount())) {
            markPaid(payment.getDate(), transaction);
        }
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
}
