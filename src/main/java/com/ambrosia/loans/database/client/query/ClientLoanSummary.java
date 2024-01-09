package com.ambrosia.loans.database.client.query;

import com.ambrosia.loans.database.loan.DLoan;
import com.ambrosia.loans.discord.base.emerald.Emeralds;
import java.util.List;

public class ClientLoanSummary {

    private final List<DLoan> loans;

    public ClientLoanSummary(List<DLoan> loans) {
        this.loans = loans;
    }

    public Emeralds getTotalOwed() {
        long amount = loans.stream()
            .map(DLoan::getTotalOwed)
            .mapToLong(Emeralds::amount)
            .sum();
        return Emeralds.of(amount);
    }

}
