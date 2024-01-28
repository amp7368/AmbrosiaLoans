package com.ambrosia.loans.database.account.event.base;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.discord.base.exception.InvalidStaffConductorException;
import com.ambrosia.loans.discord.request.base.BaseActiveRequestInvest;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AccountEventInvest extends AccountEvent implements IAccountChange {

    @Column(nullable = false)
    private long amount;

    public AccountEventInvest(DClient account, Instant date, DStaffConductor conductor, Emeralds amount, AccountEventType eventType) {
        super(account, date, conductor, eventType);
        if (eventType != AccountEventType.INVEST && eventType != AccountEventType.WITHDRAWAL) {
            String msg = "Investment must be %s or %s"
                .formatted(AccountEventType.INVEST.toString(),
                    AccountEventType.WITHDRAWAL.toString());
            throw new IllegalStateException(msg);
        }
        this.amount = amount.amount();
    }

    public AccountEventInvest(BaseActiveRequestInvest<?> request, Instant timestamp) throws InvalidStaffConductorException {
        super(request.getClient(), timestamp, request.getConductor(), request.getEventType());
        this.amount = request.getAmount().amount();
    }

    @Override
    public Instant getDate() {
        return this.date.toInstant();
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


}
