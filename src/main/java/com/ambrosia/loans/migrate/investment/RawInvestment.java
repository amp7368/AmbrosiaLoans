package com.ambrosia.loans.migrate.investment;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.balance.query.QDAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.investment.DInvestment;
import com.ambrosia.loans.database.account.event.withdrawal.DWithdrawal;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.staff.DStaffConductor;
import com.ambrosia.loans.migrate.client.ImportedClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import io.ebean.DB;
import io.ebean.Transaction;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class RawInvestment {

    private long id;
    private long clientId;
    private double amountLE;
    private Date date;
    private RawInvestmentType eventType;
    private ImportedClient client;
    private int offset;

    private Emeralds getBalanceAt(Instant date) {
        DAccountSnapshot snapshot = new QDAccountSnapshot().where()
            .date.le(Timestamp.from(date))
            .client.eq(client.getDB())
            .order().date.desc()
            .setMaxRows(1)
            .findOne();
        if (snapshot == null) return Emeralds.zero();
        return snapshot.getAccountBalance();
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void confirm() {
        Instant date = this.getDate().plusSeconds(offset);
        Emeralds realBal = getBalanceAt(date);
        try (Transaction transaction = DB.beginTransaction()) {
            Emeralds difference = this.getAmount().add(realBal.negative());
            DClient client = this.getClient();
            AccountEventType type;
            if (!difference.isNegative()) {
                type = AccountEventType.ADJUST_UP;
                DInvestment investment = new DInvestment(client, date, DStaffConductor.MIGRATION, difference, type);
                investment.save(transaction);
            } else {
                type = AccountEventType.ADJUST_DOWN;
                DWithdrawal withdrawal = new DWithdrawal(client, date, DStaffConductor.MIGRATION, difference, type);
                withdrawal.save(transaction);
            }
//            client.updateBalance(difference.amount(), date, type, transaction);
            transaction.commit();
        }
    }

    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public Instant getDate() {
        return date.toInstant().plusSeconds(1 + offset);
    }

    public RawInvestmentType getEventType() {
        return eventType;
    }

    public Emeralds getAmount() {
        return Emeralds.leToEmeralds(this.amountLE);
    }

    public DClient getClient() {
        return this.client.getDB();
    }

    public void setClient(List<ImportedClient> clients) {
        for (ImportedClient client : clients) {
            if (client.getId() == this.clientId) {
                setClient(client);
                return;
            }
        }
        throw new IllegalStateException("%d loan cannot find client %d".formatted(this.id, clientId));
    }

    private void setClient(ImportedClient client) {
        this.client = client;
        client.checkDateCreated(this.getDate());
    }
}
