package com.ambrosia.loans.database.bank.query;

import com.ambrosia.loans.util.emerald.Emeralds;

public class CachedEmeraldsQueryResult extends CachedQueryResult<Emeralds> {

    public Emeralds result(Long value) {
        if (value == null)
            return result(Emeralds.zero());
        else return result(Emeralds.of(value));
    }
}
