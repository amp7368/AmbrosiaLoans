package com.ambrosia.loans.database.account.adjust;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.base.IAccountChange;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.comment.Commentable;
import com.ambrosia.loans.database.message.comment.DComment;
import com.ambrosia.loans.database.system.exception.InvalidStaffConductorException;
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

    @OneToMany
    private final List<DComment> comments = new ArrayList<>();
    @ManyToOne(optional = false)
    private final DLoan loan;
    @Column(nullable = false)
    private final long amount;
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


    public DAdjustLoan(DLoan loan, Instant date,
        DStaffConductor staff, Emeralds amount, AccountEventType type) {
        this.date = Timestamp.from(date);
        this.conductor = staff;
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

    public Emeralds getAmount() {
        return Emeralds.of(amount);
    }

    @Override
    public long getId() {
        return this.id;
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
        this.getClient().updateBalance(this.loan, this.amount, this.getDate(), getEventType());
    }

    @Override
    public AccountEventType getEventType() {
        return type;
    }
}
