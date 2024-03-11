package com.ambrosia.loans.database.system.exception;

import com.ambrosia.loans.util.emerald.Emeralds;

public class NotEnoughFundsException extends Exception {

    private final Emeralds emeralds;
    private final Emeralds balance;

    public NotEnoughFundsException(Emeralds emeralds, Emeralds balance) {
        super(createMsg(emeralds, balance));
        this.emeralds = emeralds;
        this.balance = balance;
    }

    private static String createMsg(Emeralds emeralds, Emeralds balance) {
        return "Not enough emeralds! Tried withdrawing %s from %s investment".formatted(emeralds, balance);
    }
}
