package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.loan.payment.DLoanPayment;
import com.ambrosia.loans.database.loan.query.LoanAccess;
import com.ambrosia.loans.database.loan.section.DLoanSection;
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
import javax.persistence.Table;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "loan")
public class DLoan extends Model implements LoanAccess<DLoan> {

    @Id
    @Identity
    private long id;
    @ManyToOne
    private DClient client;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DLoanSection> sections;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DLoanPayment> payments;
    @Column
    private long amount;
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;
    @Column(nullable = false)
    private DLoanStatus status;
    @Column(nullable = false)
    private long brokerId;

    public DLoan(DClient client, long amount, double rate, long brokerId) {
        this.client = client;
        this.amount = amount;
        this.brokerId = brokerId;
        Instant now = Instant.now();
        this.startDate = Timestamp.from(now);
        this.sections = List.of(new DLoanSection(this, rate, now));
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
        amount = amount.add(payment.getEffectiveAmount(duration, sectionRate));
        return amount;
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
        BigDecimal amount = BigDecimal.valueOf(this.amount);
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
}
