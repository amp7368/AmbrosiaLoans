package com.ambrosia.loans.migrate.investment;

import com.ambrosia.loans.database.account.event.investment.InvestApi;
import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.migrate.base.RawMakeAdjustment;
import com.ambrosia.loans.migrate.client.ImportedClient;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class RawInvestment implements RawMakeAdjustment {

    private long id;
    private long clientId;
    private double amountLE;
    private Date date;
    private RawInvestmentType eventType;
    private ImportedClient client;
    private int offset;
    private DClient clientDB;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void createAdjustment(Emeralds difference, Instant date) {
        InvestApi.createAdjustment(difference, this.client(), date, false);
    }

    public long getClientId() {
        return clientId;
    }

    @Override
    public Emeralds getBalanceAt(Instant date) {
        return client().getInvestBalance(date);
    }

    @Override
    public Instant date() {
        return date.toInstant()
            .plusSeconds(1 + offset);
    }

    public RawInvestmentType getEventType() {
        return eventType;
    }


    @Override
    public Emeralds amount() {
        return Emeralds.leToEmeralds(this.amountLE);
    }

    @Override
    public DClient client() {
        if (this.clientDB != null) return clientDB;
        return this.clientDB = this.client.getDB();
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
        client.checkDateCreated(this.date());
    }
}
