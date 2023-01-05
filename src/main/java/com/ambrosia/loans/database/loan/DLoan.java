package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.collateral.DCollateral;
import io.ebean.Model;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.Identity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "loan")
public class DLoan extends Model implements LoanAccess<DLoan> {

    @Id
    @Identity
    int id;

    @ManyToOne
    DClient client;

    @OneToMany
    List<DCollateral> collateral;

    @Column(nullable = false)
    int amount;
    @Column(nullable = false)
    double rate;
    @Column(nullable = false)
    Timestamp startDate;
    @Column
    Timestamp endDate;
    @Column(nullable = false)
    DLoanStatus status;
    @Column(nullable = false)
    long brokerId;
    @DbJsonB
    LoanMoment moment;

    public DLoan() {
    }

    public DLoan(DClient client, List<DCollateral> collateral, int amount, double rate, long brokerId) {
        this.client = client;
        this.collateral = collateral;
        this.amount = amount;
        this.rate = rate;
        this.brokerId = brokerId;
        this.startDate = Timestamp.from(Instant.now());
        this.endDate = null;
        this.status = DLoanStatus.ACTIVE;
        this.moment = new LoanMoment(amount);
    }

    @Override
    public DLoan getEntity() {
        return this;
    }

    @Override
    public DLoan getSelf() {
        return this;
    }


    public DLoan setClient(DClient client) {
        this.client = client;
        return this;
    }

    public DLoan setCollateral(List<DCollateral> collateral) {
        this.collateral = collateral;
        return this;
    }

    public DLoan setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public DLoan setRate(double rate) {
        this.rate = rate;
        return this;
    }

    public DLoan setStartDate(Timestamp startDate) {
        this.startDate = startDate;
        return this;
    }

    public DLoan setEndDate(Timestamp endDate) {
        this.endDate = endDate;
        return this;
    }

    public DLoan setStatus(DLoanStatus status) {
        this.status = status;
        return this;
    }

    public DLoan setBrokerId(long brokerId) {
        this.brokerId = brokerId;
        return this;
    }

    public DLoan setMoment(LoanMoment moment) {
        this.moment = moment;
        return this;
    }
}
