package com.ambrosia.loans.database.entity.client.balance;

import com.ambrosia.loans.util.emerald.Emeralds;

public record BalanceWithInterest(Emeralds investBalance, Emeralds loanBalance, Emeralds interestAsNegative) {

    public boolean hasInterest() {
        return interestAsNegative.amount() != 0;
    }


    public long total() {
        return investBalance.add(loanBalance).add(interestAsNegative).amount();
    }

    public Emeralds totalEmeralds() {
        return Emeralds.of(total());
    }

    public long investTotal() {
        return investBalance.amount();
    }

    /**
     * includes interest
     *
     * @return interest + loanBalance
     */
    public long loanTotal() {
        return loanBalance.add(interestAsNegative).amount();
    }

}
