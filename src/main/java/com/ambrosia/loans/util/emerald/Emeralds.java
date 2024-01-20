package com.ambrosia.loans.util.emerald;

import java.math.BigDecimal;

public final class Emeralds {

    public static final int STACK = (int) Math.pow(64, 3);
    public static final int LIQUID = (int) Math.pow(64, 2);
    public static final int BLOCK = 64;
    private final long amount;

    private Emeralds(long amount) {
        this.amount = amount;
    }

    public static Emeralds of(long amount) {
        return new Emeralds(amount);
    }

    public static Emeralds leToEmeralds(double le) {
        return of((long) (LIQUID * le));
    }

    public static Emeralds of(BigDecimal amount) {
        return of(amount.longValue());
    }

    public long amount() {
        return amount;
    }

    @Override
    public String toString() {
        return EmeraldsFormatter.of()
            .setBold(false)
            .format(this);
    }

    public Emeralds negative() {
        return of(-this.amount);
    }

    public Emeralds add(long addedAmount) {
        return of(this.amount + addedAmount);
    }

    public Emeralds add(Emeralds addedAmount) {
        return of(this.amount + addedAmount.amount());
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(this.amount);
    }
}
