package com.ambrosia.loans.database.account.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.message.Commentable;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.Model;
import io.ebean.annotation.History;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@History
@MappedSuperclass
public abstract class AccountEvent extends Model implements Commentable, IAccountChange {

    @Id
    protected long id;
    @Column(nullable = false)
    @ManyToOne(optional = false)
    protected DClient client;
    @Column(nullable = false)
    protected DStaffConductor conductor;
    @Index
    @Column(nullable = false)
    protected Timestamp date;
    @Index
    @Column(nullable = false, columnDefinition = AccountEventType.DEFINITION)
    protected AccountEventType eventType;
    @Column(nullable = false)
    protected long amount;


    public AccountEvent(DClient client, Instant date, DStaffConductor conductor, Emeralds amount, AccountEventType eventType) {
        this.client = client;
        this.date = Timestamp.from(date);
        this.conductor = conductor;
        this.eventType = eventType;
        this.amount = amount.amount();
    }

    public AccountEvent(BaseActiveRequestInvest<?> request, Instant timestamp) throws InvalidStaffConductorException {
        this(request.getClient(), timestamp, request.getConductor(), request.getAmount(), request.getEventType());
        this.amount = request.getAmount().amount();
    }

    @Override
    public Instant getDate() {
        return this.date.toInstant();
    }

    public void setDate(Instant value) {
        this.date = Timestamp.from(value);
    }

    @Override
    public void updateSimulation() {
        this.client.updateBalance(this.amount, this.getDate(), getEventType());
    }

    @Override
    public AccountEventType getEventType() {
        return this.eventType;
    }

    public Emeralds getDeltaAmount() {
        return Emeralds.of(this.amount);
    }

    public void setDeltaAmount(Emeralds amount) {
        this.amount = amount.amount();
    }

    public long getId() {
        return this.id;
    }

    public DClient getClient() {
        return client;
    }

}
