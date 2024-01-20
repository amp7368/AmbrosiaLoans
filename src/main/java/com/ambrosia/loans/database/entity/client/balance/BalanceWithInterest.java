package com.ambrosia.loans.database.entity.client.balance;

import com.ambrosia.loans.util.emerald.Emeralds;

public record BalanceWithInterest(Emeralds balance, Emeralds interestAsNegative) {

    public boolean hasInterest() {
        return interestAsNegative.amount() != 0;
    }

    public long total() {
        return balance.amount() + interestAsNegative.amount();
    }
}
