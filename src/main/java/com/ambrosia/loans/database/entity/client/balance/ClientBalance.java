package com.ambrosia.loans.database.entity.client.balance;

import com.ambrosia.loans.util.emerald.Emeralds;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.jetbrains.annotations.NotNull;

@Embeddable
public class ClientBalance {

    @Column
    private long amount = 0;
    @Column
    private Timestamp lastUpdated = Timestamp.from(Instant.EPOCH);

    public void setBalance(long amount, Instant date) {
        this.amount = amount;
        this.lastUpdated = Timestamp.from(date);
    }

    @NotNull
    public Instant getLastUpdated() {
        return this.lastUpdated.toInstant();
    }

    public Emeralds getAmount() {
        return Emeralds.of(amount);
    }
}
