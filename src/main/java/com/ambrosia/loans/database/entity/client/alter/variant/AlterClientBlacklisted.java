package com.ambrosia.loans.database.entity.client.alter.variant;

import com.ambrosia.loans.database.alter.gson.AlterRecordType;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.alter.AlterClient;
import io.ebean.Transaction;
import java.util.List;

public class AlterClientBlacklisted extends AlterClient<Boolean> {

    public AlterClientBlacklisted(DClient client, boolean current) {
        super(AlterRecordType.CLIENT_BLACKLISTED, client, client.isBlacklisted(), current);
    }

    // todo
    public List<String> impactedFields() {
        return List.of("blacklisted");
    }

    @Override
    public void apply(DClient client, Boolean value, Transaction transaction) {
        client.setBlacklisted(value);
        client.save(transaction);
    }
}
