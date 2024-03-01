package com.ambrosia.loans.database.account.adjust;

import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.database.message.DComment;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
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
@Table(name = "adjust_loan")
public class DAdjustLoan extends Model implements Commentable, IAccountChange {

    @Id
    protected long id;
    @Column(nullable = false)
    protected DStaffConductor conductor;
    @Index
    @Column(nullable = false)
    protected Timestamp date;
    @Index
    @Column(nullable = false)
    protected AccountEventType type;

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();
    @ManyToOne(optional = false)
    private final DLoan loan;

    @Column(nullable = false)
    private final long amount;


    public DAdjustLoan(DLoan loan, Instant date,
        DStaffConductor conductor, Emeralds amount, AccountEventType type) {
        this.date = Timestamp.from(date);
        this.conductor = conductor;
        this.amount = amount.amount();
        this.loan = loan;
        this.type = type;
    }

    public DAdjustLoan(DLoan loan, BaseActiveRequestInvest<?> request, Instant timestamp)
        throws InvalidStaffConductorException {
        this.date = Timestamp.from(timestamp);
        this.conductor = request.getConductor();
        this.amount = request.getAmount().amount();
        this.loan = loan;
        this.type = request.getEventType();
    }

    @Override
    public List<DComment> getComments() {
        return this.comments;
    }

    @Override
    public DClient getClient() {
        return this.loan.getClient();
    }

    @Override
    public Instant getDate() {
        return date.toInstant();
    }

    @Override
    public void updateSimulation() {
        this.loan.getClient().updateBalance(this.amount, this.getDate(), getEventType());
    }

    @Override
    public AccountEventType getEventType() {
        return type;
    }

    public Emeralds getAmount() {
        return Emeralds.of(amount);
    }

    public long getId() {
        return this.id;
    }
}
