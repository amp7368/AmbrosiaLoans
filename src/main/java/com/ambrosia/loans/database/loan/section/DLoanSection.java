package com.ambrosia.loans.database.loan.section;

import com.ambrosia.loans.database.loan.DLoan;
import io.avaje.lang.Nullable;
import io.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_section")
public class DLoanSection extends Model {
    @Id
    private UUID id;
    @ManyToOne(optional = false)
    private DLoan loan;
    @Column(nullable = false)
    private double rate;
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;

    public DLoanSection(DLoan loan, double rate, Instant startDate) {
        this.loan = loan;
        this.rate = rate;
        this.startDate = Timestamp.from(startDate);
    }

    public boolean isStartBefore(Instant date) {
        return this.getStartDate().isBefore(date);
    }

    public boolean isEndBefore(Instant date) {
        Instant endDate = this.getEndDate();
        return endDate != null && endDate.isBefore(date);
    }

    public Instant getStartDate() {
        return this.startDate.toInstant();
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
}
