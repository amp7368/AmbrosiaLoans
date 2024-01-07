package com.ambrosia.loans.discord.base.emerald;

public final class Emeralds {

    private final long amount;

    private Emeralds(long amount) {
        this.amount = amount;
    }

    public static Emeralds of(long amount) {
        return new Emeralds(amount);
    }

    public long amount() {
        return amount;
    }

    @Override
    public String toString() {
        return EmeraldsFormatter.of().format(this);
    }

}
