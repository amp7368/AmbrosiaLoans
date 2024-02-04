package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.balance.DAccountSnapshot;
import com.ambrosia.loans.database.account.event.base.AccountEventType;
import com.ambrosia.loans.database.account.event.loan.DLoan;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.commands.player.profile.page.ProfileInvestPage;
import com.ambrosia.loans.discord.commands.player.profile.page.ProfileLoanPage;
import com.ambrosia.loans.discord.commands.player.profile.page.ProfileOverviewPage;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.User;

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

    boolean isBlacklisted();

    DClient setBlacklisted(boolean blacklisted);

    DClient getEntity();

    List<DLoan> getLoans();

    String getDisplayName();

    default String getEffectiveName() {
        if (this.getDisplayName() != null) return this.getDisplayName();
        String minecraft = getMinecraft(ClientMinecraftDetails::getUsername);
        if (minecraft != null) return minecraft;
        String discord = getDiscord(ClientDiscordDetails::getUsername);
        if (discord != null) return discord;
        return "error";
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

    default boolean isUser(User user) {
        Long discord = getDiscord(ClientDiscordDetails::getDiscordId);
        return discord != null && discord == user.getIdLong();
    }

    default DAccountSnapshot updateBalance(long delta, Instant timestamp, AccountEventType eventType) {
        try (Transaction transaction = DB.beginTransaction()) {
            DAccountSnapshot snapshot = updateBalance(delta, timestamp, eventType, transaction);
            transaction.commit();
            return snapshot;
        }
    }

    default DAccountSnapshot updateBalance(long delta, Instant timestamp, AccountEventType eventType, Transaction transaction) {
        DClient client = getEntity();

        BalanceWithInterest balanceWithInterest = client.getBalanceWithRecentInterest(timestamp);
        if (balanceWithInterest.hasInterest()) {
            long newBalance = balanceWithInterest.total();
            long interest = balanceWithInterest.interestAsNegative().amount();
            newSnapshot(timestamp, newBalance, interest, AccountEventType.INTEREST, transaction);
        }

        long newBalance = balanceWithInterest.total() + delta;
        DAccountSnapshot snapshot = newSnapshot(timestamp, newBalance, delta, eventType, transaction);
        client.save(transaction);

        // mark past loans as paid
        checkLoansPaid(timestamp, transaction);
        return snapshot;
    }

    private void checkLoansPaid(Instant timestamp, Transaction transaction) {
        getEntity().getLoans().stream()
            .filter(DLoan::isActive)
            .filter(loan -> loan.getStartDate().isBefore(timestamp))
            .filter(loan -> loan.getEndDate() == null)
            .forEach(loan -> loan.checkIsPaid(timestamp, transaction));
    }

    default ClientGui profile(GuiReplyFirstMessage createFirstMessage) {
        ClientGui gui = new ClientGui(this.getEntity(), DiscordBot.dcf, createFirstMessage);
        gui.addPage(new ProfileOverviewPage(gui))
            .addPage(new ProfileLoanPage(gui))
            .addPage(new ProfileInvestPage(gui));

        return gui;
    }

    default List<DAccountSnapshot> getAccountSnapshots(AccountEventType eventType) {
        return getEntity().getAccountSnapshots()
            .stream()
            .filter(snap -> snap.getEventType() == eventType)
            .toList();
    }

    default Optional<DLoan> getActiveLoan() {
        return getLoans().stream()
            .filter(loan -> loan.getStatus().isActive())
            .findFirst();
    }
}
