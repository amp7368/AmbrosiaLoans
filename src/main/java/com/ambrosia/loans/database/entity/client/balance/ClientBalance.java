package com.ambrosia.loans.database.entity.client.balance;

import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.annotation.Index;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.jetbrains.annotations.NotNull;

@Embeddable
public class ClientBalance {

    @Index
    @Column
    private long investAmount = 0;
    @Index
    @Column
    private long loanAmount = 0;
    @Column
    private long amount = 0;
    @Column
    private Timestamp lastUpdated = Timestamp.from(Instant.EPOCH);

    public void setBalance(long investAmount, long loanAmount, Instant date) {
        this.investAmount = investAmount;
        this.loanAmount = loanAmount;
        this.amount = investAmount + loanAmount;
        this.lastUpdated = Timestamp.from(date);
    }

    @NotNull
    public Instant getLastUpdated() {
        return this.lastUpdated.toInstant();
    }

    public Emeralds getInvestAmount() {
        return Emeralds.of(investAmount);
    }

    public Emeralds getLoanAmount() {
        return Emeralds.of(loanAmount);
    }
}
