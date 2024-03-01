package com.ambrosia.loans.discord.check;

public abstract class CheckError<E> {

    public abstract void checkAll(E value, CheckErrorList error);
}
