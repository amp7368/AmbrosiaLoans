package com.ambrosia.loans.database.client;

import com.ambrosia.loans.database.transaction.TransactionType;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;

@Entity
public class ClientMoment {

    public long emeraldsInvested = 0;
    public Map<TransactionType, Integer> emeraldsPerTransaction = new HashMap<>();

    public void addCredits(TransactionType transactionType, int amount) {
        emeraldsInvested += amount;
        emeraldsPerTransaction.compute(transactionType, (t, v) -> v == null ? amount : v + amount);
    }

    public long total(TransactionType profit) {
        return emeraldsPerTransaction.getOrDefault(profit, 0);
    }
}
