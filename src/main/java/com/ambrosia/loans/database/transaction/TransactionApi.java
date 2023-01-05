package com.ambrosia.loans.database.transaction;

import com.ambrosia.loans.database.client.ClientApi;
import com.ambrosia.loans.database.client.DClient;
import com.ambrosia.loans.database.transaction.query.QDTransaction;
import io.ebean.DB;
import io.ebean.Transaction;

import java.util.List;

public class TransactionApi {


    public static DTransaction createTransaction(long conductorId, DClient client, long amount, TransactionType transactionType) {
        try (Transaction transaction = DB.getDefault().beginTransaction()) {
            client = ClientApi.findById(client.id).entity; // refetch the client
            if (client == null) throw new IllegalStateException("Client " + client + " does not exist!");
            DTransaction operation = new DTransaction(conductorId, client, amount, transactionType);
            client.moment.addCredits(transactionType, amount);
            DB.getDefault().save(operation, transaction);
            DB.getDefault().update(client, transaction);
            transaction.commit();
            return operation;
        }
    }

    public static DTransaction delete(Long id) {
        DTransaction operation = DB.find(DTransaction.class, id);
        if (operation == null) return null;
        DB.delete(DTransaction.class, id);
        return operation;
    }

    public static List<DTransaction> findTransactionsByClient(DClient client) {
        return new QDTransaction().where().client.eq(client).findList();
    }
}
