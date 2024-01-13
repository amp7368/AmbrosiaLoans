package com.ambrosia.loans.database.log.invest;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.database.log.base.AccountEvent;
import com.ambrosia.loans.database.log.base.AccountEventType;
import com.ambrosia.loans.database.log.base.IAccountChange;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "invest")
public class DInvest extends AccountEvent implements IAccountChange {

    @Column(nullable = false)
    private long amount;

    public DInvest(DClient account, Instant date, DStaffConductor conductor, long amount, AccountEventType eventType) {
        super(account, date, conductor, eventType);
        if (eventType != AccountEventType.INVEST && eventType != AccountEventType.WITHDRAWAL) {
            String msg = "Investment must be %s or %s"
                .formatted(AccountEventType.INVEST.displayName(),
                    AccountEventType.WITHDRAWAL.displayName());
            throw new IllegalStateException(msg);
        }
        this.amount = amount;
    }

    @Override
    public Instant getDate() {
        return this.date.toInstant();
    }

    @Override
    public void updateSimulation() {
        this.account.updateBalance(this.amount, this.getDate(), getEventType());
    }

    @Override
    public AccountEventType getEventType() {
        return this.eventType;
    }
}