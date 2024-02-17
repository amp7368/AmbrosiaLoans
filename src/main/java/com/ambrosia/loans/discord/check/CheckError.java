package com.ambrosia.loans.discord.check;

public abstract class CheckError<E> {

    public abstract CheckErrorList checkAll(E value, CheckErrorList error);
}
