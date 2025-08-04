package com.ambrosia.loans.util.emerald;

import com.ambrosia.loans.Bank;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;

public final class Emeralds implements Comparable<Emeralds> {

    public static final long STACK = (long) Math.pow(64, 3);
    public static final long LIQUID = (long) Math.pow(64, 2);
    public static final long BLOCK = 64;
    private static final Emeralds ZERO = of(0);
    private final long amount;

    private Emeralds(long amount) {
        this.amount = amount;
    }

    public static Emeralds of(long amount) {
        return new Emeralds(amount);
    }

    public static Emeralds of(BigDecimal amount) {
        return new Emeralds(amount.longValue());
    }

    public static Emeralds zero() {
        return ZERO;
    }

    public static Emeralds leToEmeralds(double le) {
        return new Emeralds((long) (LIQUID * le));
    }

    public static Emeralds stxToEmeralds(double stx) {
        return of((long) (STACK * stx));
    }

    public long amount() {
        return amount;
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

    public Emeralds minus(long minusAmount) {
        return of(this.amount - minusAmount);
    }

    public Emeralds minus(Emeralds minusAmount) {
        return of(this.amount - minusAmount.amount());
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(this.amount);
    }

    public double toStacks() {
        BigDecimal stackAmount = BigDecimal.valueOf(Emeralds.STACK);
        return toBigDecimal()
            .divide(stackAmount, Bank.FLOOR_CONTEXT)
            .doubleValue();
    }

    public double toLiquids() {
        BigDecimal liquidAmount = BigDecimal.valueOf(Emeralds.LIQUID);
        return toBigDecimal()
            .divide(liquidAmount, Bank.FLOOR_CONTEXT)
            .doubleValue();
    }

    @Override
    public int hashCode() {
        return (int) (this.amount % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Emeralds other && this.amount == other.amount;
    }

    @Override
    public String toString() {
        return EmeraldsFormatter.of()
            .format(this);
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

    public boolean eq(long compareAmount) {
        return this.amount == compareAmount;
    }

    public boolean isNegative() {
        return this.amount < 0;
    }

    public boolean isZero() {
        return eq(0);
    }

    public boolean isPositive() {
        return gt(0);
    }

    @Override
    public int compareTo(@NotNull Emeralds o) {
        return Long.compare(this.amount, o.amount);
    }
}
