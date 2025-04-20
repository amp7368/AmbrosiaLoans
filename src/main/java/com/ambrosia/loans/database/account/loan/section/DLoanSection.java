package com.ambrosia.loans.database.account.loan.section;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.HasDateRange;
import com.ambrosia.loans.database.account.loan.InterestCheckpoint;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import io.ebean.Model;
import io.ebean.annotation.History;
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
import org.jetbrains.annotations.Nullable;

@History
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

    public boolean isEndBeforeOrEq(Instant date) {
        Instant endDate = this.getEndDate();
        return endDate != null && !date.isBefore(endDate);
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
        if (endDate == null) this.endDate = null;
        else this.endDate = Timestamp.from(endDate);
        return this;
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public BigDecimal getInterest(InterestCheckpoint checkpoint, Instant end) {
        Duration duration = getDuration(checkpoint.lastUpdated(), end);
        if (!duration.isPositive()) return BigDecimal.ZERO;
        BigDecimal rate = BigDecimal.valueOf(getRate());

        ApiVersionListLoan version = loan.getVersion().getLoan();
        if (version.equals(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY)) {
            duration = Bank.legacySimpleWeeksDuration(duration);
        }

        return Bank.interest(duration, checkpoint.principal(), rate);
    }

    public void accumulateInterest(InterestCheckpoint checkpoint, Instant end) {
        BigDecimal sectionInterest = getInterest(checkpoint, end);
        checkpoint.accumulateInterest(sectionInterest, end);
    }
}
