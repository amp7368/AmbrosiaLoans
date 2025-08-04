package com.ambrosia.loans.database.account.loan.interest.base;

import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.interest.legacy.DLegacyInterestCheckpoint;
import com.ambrosia.loans.database.account.loan.interest.standard.DStandardInterestCheckpoint;
import com.ambrosia.loans.util.emerald.Emeralds;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DStandardInterestCheckpoint.class, name = "standard"),
    @JsonSubTypes.Type(value = DLegacyInterestCheckpoint.class, name = "legacy")
})
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class InterestCheckpoint {

    protected transient DLoan loan;
    protected long balance; // positive
    protected Instant lastUpdated;
    protected long accumulatedInterest = 0;

    public InterestCheckpoint() {
    }

    public InterestCheckpoint(DLoan loan) {
        this.loan = loan;
        this.balance = loan.getInitialAmount().amount();
        this.lastUpdated = loan.getStartDate();
    }

    public InterestCheckpoint(InterestCheckpoint other) {
        this.loan = other.loan;
        this.balance = other.balance;
        this.lastUpdated = other.lastUpdated;
        this.accumulatedInterest = other.accumulatedInterest;
    }

    public void accumulateInterest(BigDecimal interest, Instant date) {
        this.accumulateInterest(interest.longValue(), date);
    }

    public void accumulateInterest(long interest, Instant date) {
        this.accumulatedInterest += interest;
        updateBalance(interest, date);
    }

    public long getInterest() {
        return accumulatedInterest;
    }

    public void updateBalance(long delta, Instant date) {
        this.balance += delta;
        this.lastUpdated = date;
    }

    public void resetInterest() {
        this.accumulatedInterest = 0;
    }

    public final long balance() {
        return balance;
    }

    public final Instant lastUpdated() {
        return lastUpdated;
    }

    public final long accumulatedInterest() {
        return accumulatedInterest;
    }

    // positive
    public final Emeralds interestEmeralds() {
        return Emeralds.of(accumulatedInterest);
    }

    // positive
    public final Emeralds balanceEmeralds() {
        return Emeralds.of(balance);
    }

    public final DLoan getLoan() {
        return loan;
    }

    public final InterestCheckpoint setLoan(DLoan loan) {
        this.loan = loan;
        return this;
    }

    public abstract InterestCheckpoint copy();
}
