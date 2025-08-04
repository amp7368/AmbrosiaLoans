package com.ambrosia.loans.database.entity.client.balance;

import com.ambrosia.loans.database.account.DClientInvestSnapshot;
import com.ambrosia.loans.database.account.DClientSnapshot;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Comparator;

public final class BalanceWithInterest {

    private final DClient client;
    private final Instant currentTime;
    private final Emeralds loanBalance;
    private final Emeralds interestAsNegative;
    private final Emeralds legacyInterestAsNegative;
    private Emeralds investBalance;

    public BalanceWithInterest(DClient client, Instant currentTime, Emeralds loanBalance, Emeralds interestAsNegative,
        Emeralds legacyInterestAsNegative) {
        this.client = client;
        this.currentTime = currentTime;
        this.loanBalance = loanBalance;
        this.interestAsNegative = interestAsNegative;
        this.legacyInterestAsNegative = legacyInterestAsNegative;
    }

    public boolean hasInterest() {
        return interestAsNegative.amount() != 0;
    }

    public boolean hasLegacyInterest() {
        return legacyInterestAsNegative.amount() != 0;
    }

    public Emeralds totalEmeralds() {
        return investBalance()
            .add(loanBalance())
            .add(interestAsNegative());
    }

    public Emeralds investTotal() {
        return investBalance();
    }

    /**
     * loan balance as negative (includes interest)
     *
     * @return interest + loanBalance
     */
    public Emeralds loanTotal() {
        return loanBalance.add(interestAsNegative).add(legacyInterestAsNegative);
    }

    public Emeralds investBalance() {
        if (investBalance != null) return investBalance;
        return investBalance = client.getInvestSnapshots().stream()
            .filter(s -> !s.getDate().isAfter(currentTime))
            .max(Comparator.comparing(DClientInvestSnapshot::getDate))
            .map(DClientSnapshot::getBalance)
            .orElse(Emeralds.zero());
    }

    public Emeralds loanBalance() {
        return loanBalance;
    }

    public Emeralds interestAsNegative() {
        return interestAsNegative;
    }

    public Emeralds legacyInterestAsNegative() {
        return legacyInterestAsNegative;
    }
}
