package com.ambrosia.loans.database.account.loan;

import com.ambrosia.loans.database.account.DClientLoanSnapshot;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.math.BigDecimal;
import java.time.Instant;

public class InterestCheckpoint {

    private final DLoan loan;
    private long principal; // positive
    private long balance; // positive
    private Instant lastUpdated;
    private long accumulatedInterest = 0;

    public InterestCheckpoint(DClientLoanSnapshot snapshot) {
        this.lastUpdated = snapshot.getDate();
        this.loan = snapshot.getLoan();
        this.principal = -snapshot.getPrincipal();
        this.balance = -snapshot.getBalance().amount();
    }

    public InterestCheckpoint(DLoan loan) {
        this.loan = loan;
        this.principal = loan.getInitialAmount().amount();
        this.balance = this.principal;
        this.lastUpdated = loan.getStartDate();
    }

    public InterestCheckpoint(InterestCheckpoint other) {
        this.loan = other.loan;
        this.principal = other.principal;
        this.balance = other.balance;
        this.lastUpdated = other.lastUpdated;
        this.accumulatedInterest = other.accumulatedInterest;
    }

    public void accumulateInterest(BigDecimal interest, Instant date) {
        long interestLong = interest.longValue();
        this.accumulatedInterest += interestLong;
        updateBalance(interestLong, date);
    }

    public void updateNegativeBalance(long delta, Instant timestamp) {
        updateBalance(-delta, timestamp);
    }

    public long addInterest() {
        long delta = accumulatedInterest;
        resetInterest();
        return delta;
    }

    public void updateBalance(long delta, Instant date) {
        this.balance += delta;
        this.lastUpdated = date;
        this.principal = Math.min(this.principal, this.balance);
        this.principal = Math.max(this.principal, 0);
    }

    public void resetInterest() {
        this.accumulatedInterest = 0;
    }

    public long balance() {
        return balance;
    }

    public BigDecimal principal() {
        return BigDecimal.valueOf(principal);
    }

    public Instant lastUpdated() {
        return lastUpdated;
    }

    public long accumulatedInterest() {
        return accumulatedInterest;
    }

    public Emeralds interestEmeralds() {
        return Emeralds.of(accumulatedInterest);
    }

    public Emeralds balanceEmeralds() {
        return Emeralds.of(balance);
    }

    @Override
    public String toString() {
        return "InterestCheckpoint{" +
            "principal=" + Emeralds.of(principal) +
            ", balance=" + Emeralds.of(balance) +
            ", lastUpdated=" + lastUpdated +
            ", accumulatedInterest=" + Emeralds.of(accumulatedInterest) +
            '}';
    }

    public DLoan getLoan() {
        return loan;
    }

    public void addToPrincipal(long delta) {
        this.principal += delta;
    }

    public InterestCheckpoint copy() {
        return new InterestCheckpoint(this);
    }
}
