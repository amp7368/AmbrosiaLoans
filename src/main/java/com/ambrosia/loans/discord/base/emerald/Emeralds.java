package com.ambrosia.loans.discord.base.emerald;

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

    public long amount() {
        return amount;
    }

    @Override
    public String toString() {
        return EmeraldsFormatter.of().format(this);
    }

}
