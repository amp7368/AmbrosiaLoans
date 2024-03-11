package com.ambrosia.loans.database.system.exception;

import com.ambrosia.loans.util.emerald.Emeralds;

public class OverpaymentException extends Exception {

    private final Emeralds emeralds;
    private final Emeralds balance;

    public OverpaymentException(Emeralds emeralds, Emeralds balance) {
        super(createMsg(emeralds, balance));
        this.emeralds = emeralds;
        this.balance = balance;
    }

    private static String createMsg(Emeralds emeralds, Emeralds balance) {
        return "Cannot overpay! Tried paying %s on %s loan.".formatted(emeralds, balance);
    }
}
