package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.loan.collateral.DCollateral;
import com.ambrosia.loans.database.loan.query.LoanAccess;
import com.ambrosia.loans.database.loan.section.DLoanSection;
import io.ebean.Model;
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
    private long id;
    @ManyToOne
    private DClient client;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DLoanSection> sections;
    @Column
    private long amount;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DCollateral> collateral;
    @Column(nullable = false)
    private Timestamp startDate;
    @Column
    private Timestamp endDate;
    @Column(nullable = false)
    private DLoanStatus status;
    @Column(nullable = false)
    private long brokerId;

    public DLoan() {
    }

    public DLoan(DClient client, int amount, List<DCollateral> collateral, long brokerId) {
        this.client = client;
        this.amount = amount;
        this.collateral = collateral;
        this.brokerId = brokerId;
        this.startDate = Timestamp.from(Instant.now());
        this.status = DLoanStatus.ACTIVE;
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

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public List<DLoanSection> getSections() {
        return this.sections;
    }

    public void setSections(List<DLoanSection> sections) {
        this.sections = sections;
    }

    public void addSection(DLoanSection section) {
        this.sections.add(section);
    }
}
