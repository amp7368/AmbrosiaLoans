package com.ambrosia.loans.database.account;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.entity.client.DClient;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "client_invest_snapshot")
public class DClientInvestSnapshot extends DClientSnapshot {

    public DClientInvestSnapshot(DClient client, Instant date, long balance, long delta, AccountEventType event) {
        super(client, date, balance, delta, event);
    }
}
