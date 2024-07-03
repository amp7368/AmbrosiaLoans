package com.ambrosia.loans.database.entity.client.balance;

import com.ambrosia.loans.util.emerald.Emeralds;

public record BalanceWithInterest(Emeralds investBalance, Emeralds loanBalance, Emeralds interestAsNegative,
                                  Emeralds legacyInterestAsNegative) {

    public boolean hasInterest() {
        return interestAsNegative.amount() != 0;
    }

    public boolean hasLegacyInterest() {
        return legacyInterestAsNegative.amount() != 0;
    }

    public Emeralds totalEmeralds() {
        return Emeralds.of(investBalance.add(loanBalance).add(interestAsNegative).amount());
    }

    public Emeralds investTotal() {
        return investBalance;
    }

    /**
     * loan balance as negative (includes interest)
     *
     * @return interest + loanBalance
     */
    public Emeralds loanTotal() {
        return loanBalance.add(interestAsNegative).add(legacyInterestAsNegative);
    }
}
