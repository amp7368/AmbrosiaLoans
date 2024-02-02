package com.ambrosia.loans.database.account.event.loan.section;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.account.event.loan.HasDateRange;
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
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "loan_section")
public class DLoanSection extends Model implements HasDateRange {

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

    @NotNull
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

    public DLoanSection setEndDate(Instant endDate) {
        this.endDate = Timestamp.from(endDate);
        return this;
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public BigDecimal getInterest(Instant start, Instant end, BigDecimal principal) {
        Duration duration = getDuration(start, end);
        if (duration.isNegative()) return BigDecimal.ZERO;
        BigDecimal rate = BigDecimal.valueOf(getRate());
        return Bank.interest(duration, principal, rate);
    }
}
