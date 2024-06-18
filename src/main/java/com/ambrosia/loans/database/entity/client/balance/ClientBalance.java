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
    private Timestamp loanLastUpdated = Timestamp.from(Instant.EPOCH);
    @Column
    private Timestamp investLastUpdated = Timestamp.from(Instant.EPOCH);

    public Emeralds addInvestBalance(long investDelta, Instant date) {
        this.investAmount += investDelta;
        this.amount = this.investAmount + this.loanAmount;
        this.investLastUpdated = Timestamp.from(date);
        return Emeralds.of(this.investAmount);
    }

    public Emeralds addLoanBalance(long loanDelta, Instant date) {
        this.loanAmount += loanDelta;
        this.amount = this.investAmount + this.loanAmount;
        this.loanLastUpdated = Timestamp.from(date);
        return Emeralds.of(this.loanAmount);
    }

    @NotNull
    public Instant getLoanLastUpdated() {
        return this.loanLastUpdated.toInstant();
    }

    public Emeralds getInvestAmount() {
        return Emeralds.of(investAmount);
    }

    public Emeralds getLoanAmount() {
        return Emeralds.of(loanAmount);
    }
}
