package com.ambrosia.loans.database.loan;

import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.collateral.DCollateral;
import io.ebean.Model;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "loan")
public class DLoan extends Model {

    @Id
    @Identity
    public int id;

    @ManyToOne
    public DClient client;

    @OneToMany
    public List<DCollateral> collateral;

    @Column(nullable = false)
    public int amount;
    @Column(nullable = false)
    public double rate;
    @Column(nullable = false)
    public Timestamp startDate;
    @Column
    public Timestamp endDate;
    @Column(nullable = false)
    public DLoanStatus status;
    @DbJsonB
    public LoanMoment moment;

    public DLoan(DClient client, List<DCollateral> collateral, int amount, double rate) {
        this.client = client;
        this.collateral = collateral;
        this.amount = amount;
        this.rate = rate;
        this.startDate = Timestamp.from(Instant.now());
        this.endDate = null;
        this.status = DLoanStatus.ACTIVE;
        this.moment = new LoanMoment(amount);
    }
}
