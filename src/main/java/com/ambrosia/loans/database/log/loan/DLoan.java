package com.ambrosia.loans.database.log.loan;

import com.ambrosia.loans.database.base.util.CreateEntityException;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.base.IAccountChange;
import com.ambrosia.loans.database.log.loan.collateral.DCollateral;
import com.ambrosia.loans.database.log.loan.comment.DLoanComment;
import com.ambrosia.loans.database.log.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.log.loan.query.LoanAccess;
import com.ambrosia.loans.database.log.loan.query.LoanApi;
import com.ambrosia.loans.database.log.loan.section.DLoanSection;
import com.ambrosia.loans.discord.active.cash.ActiveRequestLoan;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
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
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "loan")
public class DLoan extends Model implements LoanAccess<DLoan>, IAccountChange {

    @Id
    @Identity
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
    private List<DLoanComment> comments;

    public DLoan(DClient client, long initialAmount, double rate, DStaffConductor conductor) {
        this.client = client;
        this.initialAmount = initialAmount;
        this.conductor = conductor;
        Instant now = Instant.now();
        this.startDate = Timestamp.from(now);
        this.sections = List.of(new DLoanSection(this, rate, now));
        this.status = DLoanStatus.ACTIVE;
    }

    public DLoan(ActiveRequestLoan request) throws CreateEntityException {
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

    @Override
    public DLoan getEntity() {
        return this;
    }

    @Override
    public DLoan getSelf() {
        return this;
    }

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
        return this.sections.stream().sorted(Comparator.comparing(DLoanSection::getStartDate, Instant::compareTo)).toList();
    }

    public Emeralds getTotalOwed() {
        BigDecimal amount = BigDecimal.valueOf(this.initialAmount);
        int paymentIndex = 0;
        for (DLoanSection section : getSections()) {
            amount = amount.add(section.getInterest(amount));

            while (paymentIndex < payments.size()) {
                DLoanPayment payment = payments.get(paymentIndex);
                BigDecimal paymentAmount = calcPayment(section, payment, amount);
                if (paymentAmount == null) break;
                amount = amount.subtract(paymentAmount);
                paymentIndex++;
            }
        }
        return Emeralds.of(amount.longValue());
    }

    public DClient getClient() {
        return this.client;
    }

    public void makePayment(DLoanPayment payment) {
        this.payments.add(payment);
        if (getTotalOwed().amount() < Emeralds.BLOCK) {
            this.status = DLoanStatus.PAID;
        }
    }

    public long getId() {
        return this.id;
    }

    public List<DLoanPayment> getPayments() {
        return this.payments;
    }

    public LoanApi api() {
        return LoanApi.api(this);
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
}
