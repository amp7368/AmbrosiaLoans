package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.database.entity.client.query.ClientLoanSummary;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.commands.player.profile.ProfileGui;
import com.ambrosia.loans.discord.commands.player.profile.ProfileInvestPage;
import com.ambrosia.loans.discord.commands.player.profile.ProfileLoanPage;
import com.ambrosia.loans.discord.commands.player.profile.ProfileOverviewPage;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public interface ClientAccess {

    private DAccountSnapshot newSnapshot(Instant timestamp, long newBalance, long interest,
        AccountEventType eventType, Transaction transaction) {
        DClient client = getEntity();
        client.setBalance(newBalance, timestamp);
        DAccountSnapshot snapshot = new DAccountSnapshot(client, timestamp, newBalance, interest, eventType);
        client.addAccountSnapshot(snapshot);
        snapshot.save(transaction);
        return snapshot;
    }

    DClient getEntity();

    List<DLoan> getLoans();

    default boolean hasAnyTransactions() {
        return true; // todo
    }

    default <T> T getMinecraft(Function<ClientMinecraftDetails, T> apply) {
        ClientMinecraftDetails minecraft = getEntity().getMinecraft();
        if (minecraft == null) return null;
        return apply.apply(minecraft);
    }

    default <T> T getDiscord(Function<ClientDiscordDetails, T> apply) {
        ClientDiscordDetails discord = getEntity().getDiscord();
        if (discord == null) return null;
        return apply.apply(discord);
    }

    default DAccountSnapshot updateBalance(long amount, Instant timestamp, AccountEventType eventType) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAccountSnapshot snapshot = updateBalance(amount, timestamp, eventType, transaction);
            transaction.commit();
            return snapshot;
        }
    }

    default DAccountSnapshot updateBalance(long delta, Instant timestamp, AccountEventType eventType, Transaction transaction) {
        DClient client = getEntity();

        BalanceWithInterest balanceWithInterest = client.getBalanceWithInterest(timestamp);
        if (balanceWithInterest.hasInterest()) {
            long newBalance = balanceWithInterest.total();
            long interest = balanceWithInterest.interestAsNegative().amount();
            newSnapshot(timestamp, newBalance, interest, AccountEventType.INTEREST, transaction);
        }

        long newBalance = balanceWithInterest.total() + delta;
        DAccountSnapshot snapshot = newSnapshot(timestamp, newBalance, delta, eventType, transaction);
        client.save(transaction);
        return snapshot;
    }

    default ClientLoanSummary getLoanSummary() {
        return new ClientLoanSummary(getEntity().getLoans());
    }

    default ProfileGui profile(GuiReplyFirstMessage createFirstMessage) {
        ProfileGui gui = new ProfileGui(this.getEntity(), DiscordBot.dcf, createFirstMessage);
        gui.addPage(new ProfileOverviewPage(gui))
            .addPage(new ProfileLoanPage(gui))
            .addPage(new ProfileInvestPage(gui));
        return gui;
    }

}
