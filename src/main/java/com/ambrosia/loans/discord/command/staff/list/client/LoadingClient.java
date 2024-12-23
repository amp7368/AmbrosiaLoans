package com.ambrosia.loans.discord.command.staff.list.client;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;

public class LoadingClient {

    private final DClient client;
    private boolean isLoaded = false;
    private BalanceWithInterest balance;

    public LoadingClient(DClient client) {
        this.client = client;
    }

    public void load() {
        this.balance = client.getBalanceWithRecentInterest(Instant.now());

        synchronized (this) {
            this.isLoaded = true;
        }
    }

    public boolean isLoaded() {
        synchronized (this) {
            return isLoaded;
        }
    }

    public DClient client() {
        return client;
    }

    public Emeralds getInvestBalance() {
        return this.balance.investBalance();
    }

    public Emeralds getLoanAmount() {
        return this.balance.loanBalance().negative();
    }

    public boolean hasActiveLoan() {
        return this.client.getActiveLoan().isPresent();
    }

    public boolean isInvestor() {
        if (!isLoaded) return true;
        return getInvestBalance().isPositive();
    }

    public boolean isBlacklisted() {
        return client.isBlacklisted();
    }

    public Instant joinDate() {
        return client.getDateCreated();
    }

    public boolean isZero() {
        return !isInvestor() && !hasActiveLoan();
    }
}
