package com.ambrosia.loans.database.loan.section;

import com.ambrosia.loans.bank.Bank;
import com.ambrosia.loans.database.loan.DLoan;
import io.avaje.lang.Nullable;
import io.ebean.Model;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "loan_section")
public class DLoanSection extends Model {

    @Id
    private UUID id;
    @ManyToOne(optional = false)
    private DLoan loan;
    @Column(nullable = false)
    private double rate; // between 0 and 1
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;

    public DLoanSection(DLoan loan, double rate, Instant startDate) {
        this.loan = loan;
        this.rate = rate;
        this.startDate = Timestamp.from(startDate);
    }

    public boolean isEndBefore(Instant date) {
        Instant endDate = this.getEndDate();
        return endDate != null && endDate.isBefore(date);
    }

    public Instant getStartDate() {
        return this.startDate.toInstant();
    }

    public void setStartDate(Instant startDate) {
        this.startDate = Timestamp.from(startDate);
    }

    @Nullable
    public Instant getEndDate() {
        return this.endDate == null ? null : this.endDate.toInstant();
    }

    public void setEndDate(Instant endDate) {
        this.endDate = Timestamp.from(endDate);
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public BigDecimal getInterest(BigDecimal amount) {
        BigDecimal rate = BigDecimal.valueOf(getRate());
        return Bank.interest(getDuration(), amount, rate);
    }

    private Duration getDuration() {
        Instant end = getEndDate();
        if (end == null) end = Instant.now();
        return Duration.between(getStartDate(), end);
    }
}
