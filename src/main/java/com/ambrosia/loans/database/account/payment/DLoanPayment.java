package com.ambrosia.loans.database.account.payment;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.comment.Commentable;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.History;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@History
@Entity
@Table(name = "loan_payment")
public class DLoanPayment extends Model implements Commentable {

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();
    @Id
    private long id;
    @ManyToOne
    private DLoan loan;
    @Index
    @Column(nullable = false)
    private Timestamp date;
    @Column
    private long amount;
    @ManyToOne(optional = false)
    private DStaffConductor conductor;


    public DLoanPayment(DLoan loan, Instant date, long amount, DStaffConductor conductor) {
        this.loan = loan;
        this.date = Timestamp.from(date);
        this.amount = amount;
        this.conductor = conductor;
    }

    public Emeralds getAmount() {
        return Emeralds.of(this.amount);
    }

    public DLoanPayment setAmount(Emeralds amount) {
        this.amount = amount.amount();
        return this;
    }

    public Instant getDate() {
        return this.date.toInstant();
    }

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }

    public void updateSimulation() {
        DClient client = this.loan.getClient();
        client.refresh();
        client.updateBalance(amount, getDate(), AccountEventType.PAYMENT);
    }

    public DLoan getLoan() {
        return this.loan;
    }

    public long getId() {
        return this.id;
    }
}
