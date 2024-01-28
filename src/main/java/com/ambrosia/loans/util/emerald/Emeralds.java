package com.ambrosia.loans.util.emerald;

import java.math.BigDecimal;
import java.math.MathContext;

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


    public static Emeralds of(BigDecimal amount) {
        return of(amount.longValue());
    }

    public static Emeralds zero() {
        return of(0);
    }

    public static Emeralds leToEmeralds(double le) {
        return of((long) (LIQUID * le));
    }

    public static Emeralds stxToEmeralds(double stx) {
        BigDecimal emeralds = BigDecimal.valueOf(stx)
            .multiply(BigDecimal.valueOf(STACK));
        return of(emeralds);
    }


    public long amount() {
        return amount;
    }

    @Override
    public String toString() {
        return EmeraldsFormatter.of()
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

    public double toStacks() {
        BigDecimal stackAmount = BigDecimal.valueOf(Emeralds.STACK);
        return toBigDecimal()
            .divide(stackAmount, MathContext.DECIMAL128)
            .doubleValue();
    }

    public double toLiquids() {
        BigDecimal liquidAmount = BigDecimal.valueOf(Emeralds.LIQUID);
        return toBigDecimal()
            .divide(liquidAmount, MathContext.DECIMAL128)
            .doubleValue();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Emeralds other && this.amount == other.amount;
    }

    @Override
    public int hashCode() {
        return (int) (this.amount % Integer.MAX_VALUE);
    }

    public boolean isNegative() {
        return this.amount < 0;
    }

    public boolean lte(long compareAmount) {
        return this.amount <= compareAmount;
    }

    public boolean lt(long compareAmount) {
        return this.amount < compareAmount;
    }

    public boolean gte(long compareAmount) {
        return this.amount >= compareAmount;
    }

    public boolean gt(long compareAmount) {
        return this.amount > compareAmount;
    }
}
