package com.ambrosia.loans.database.account.event.payment;

import com.ambrosia.loans.Bank;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.Index;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "loan_payment")
public class DLoanPayment extends Model implements Commentable {

    @Id
    private UUID id;
    @ManyToOne
    private DLoan loan;
    @Index
    @Column(nullable = false)
    private Timestamp date;
    @Column
    private long amount;
    @ManyToOne(optional = false)
    private DStaffConductor conductor;
    @OneToMany
    private final List<DComment> comments = new ArrayList<>();


    public DLoanPayment(DLoan loan, Instant date, long amount, DStaffConductor conductor) {
        this.loan = loan;
        this.date = Timestamp.from(date);
        this.amount = amount;
        this.conductor = conductor;
    }

    public Emeralds getAmount() {
        return Emeralds.of(this.amount);
    }

    public Instant getDate() {
        return this.date.toInstant();
    }

    public BigDecimal getEffectiveAmount(Duration duration, BigDecimal rate) {
        BigDecimal amount = BigDecimal.valueOf(this.amount);
        return amount.add(Bank.interest(duration, amount, rate));
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
}
