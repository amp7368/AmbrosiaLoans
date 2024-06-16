package com.ambrosia.loans.database.account;

import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.discord.DiscordModule;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.time.Instant;
import java.time.Month;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

public class ClientMergedSnapshot implements Comparable<ClientMergedSnapshot> {

    private static final Comparator<ClientMergedSnapshot> COMPARATOR = Comparator.comparing(ClientMergedSnapshot::getDate)
        .thenComparing(ClientMergedSnapshot::getEventType, AccountEventType.ORDER);
    private Emeralds investBalance;
    private Emeralds loanBalance;
    private AccountEventType event;
    private Instant date;
    private Emeralds investDelta = Emeralds.zero();
    private Emeralds loanDelta = Emeralds.zero();

    public ClientMergedSnapshot(Instant date, Emeralds loanBalance, Emeralds investBalance) {
        this.date = date;
        this.loanBalance = loanBalance;
        this.investBalance = investBalance;
    }


    public Instant getDate() {
        return date;
    }

    public Emeralds getDelta() {
        return investDelta.add(loanDelta);
    }

    public Emeralds getBalance() {
        return investBalance.add(loanBalance);
    }

    public AccountEventType getEventType() {
        return event;
    }

    public boolean tryMerge(DClientSnapshot other) {
        if (this.event == null) {
            doMerge(other);
            return true;
        }

        if (!this.event.isProfit() || !other.getEventType().isProfit()) return false;

        Month thisMonth = this.date.atZone(DiscordModule.TIME_ZONE).getMonth();
        Month otherMonth = other.getDate().atZone(DiscordModule.TIME_ZONE).getMonth();
        if (!thisMonth.equals(otherMonth)) return false;
        doMerge(other);
        return true;
    }

    private void doMerge(DClientSnapshot other) {
        this.event = other.getEventType();
        this.date = other.getDate();
        if (other.getEventType().isLoanLike()) {
            this.loanBalance = other.getBalance();
            this.loanDelta = this.loanDelta.add(other.getDelta());
        } else {
            this.investBalance = other.getBalance();
            this.investDelta = this.investDelta.add(other.getDelta());
        }
    }

    @Override
    public int compareTo(@NotNull ClientMergedSnapshot o) {
        return COMPARATOR.compare(this, o);
    }

    public Emeralds getAccountBalance() {
        return loanBalance.add(investBalance);
    }
}
