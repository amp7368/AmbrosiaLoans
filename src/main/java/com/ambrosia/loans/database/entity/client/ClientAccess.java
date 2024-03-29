package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.DClientSnapshot;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.entity.client.balance.BalanceWithInterest;
import com.ambrosia.loans.database.entity.client.meta.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.meta.ClientMinecraftDetails;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileInvestPage;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileLoanPage;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileOverviewPage;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.dv8tion.jda.api.entities.User;

public interface ClientAccess {

    private DClientSnapshot newSnapshot(Instant timestamp,
        long newInvestBalance, long newLoanBalance, long interest,
        AccountEventType eventType, Transaction transaction) {
        DClient client = getEntity();
        client.setBalance(newInvestBalance, newLoanBalance, timestamp);

        DClientSnapshot snapshot = new DClientSnapshot(client, timestamp,
            newInvestBalance, newLoanBalance, interest,
            eventType);
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
        return "Not Found!";
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

    default DClientSnapshot updateBalance(long delta, Instant timestamp, AccountEventType eventType) {
        try (Transaction transaction = DB.beginTransaction()) {
            DClientSnapshot snapshot = updateBalance(delta, timestamp, eventType, transaction);
            transaction.commit();
            return snapshot;
        }
    }

    default DClientSnapshot updateBalance(long delta, Instant timestamp, AccountEventType eventType, Transaction transaction) {
        DClient client = getEntity();

        BalanceWithInterest balanceWithInterest = client.getBalanceWithRecentInterest(timestamp);
        long newInvestBalance = balanceWithInterest.investTotal().amount();
        long newLoanBalance = balanceWithInterest.loanTotal().amount();

        if (balanceWithInterest.hasInterest() && eventType.isLoanLike()) {
            long interest = balanceWithInterest.interestAsNegative().amount();
            newSnapshot(timestamp, newInvestBalance, newLoanBalance, interest, AccountEventType.INTEREST, transaction);
        }

        if (eventType.isLoanLike()) newLoanBalance += delta;
        else newInvestBalance += delta;

        DClientSnapshot snapshot = newSnapshot(timestamp, newInvestBalance, newLoanBalance, delta, eventType, transaction);
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

    default List<DClientSnapshot> getAccountSnapshots(Predicate<AccountEventType> test) {
        return getEntity().getAccountSnapshots()
            .stream()
            .filter(snap -> test.test(snap.getEventType()))
            .toList();
    }

    default Optional<DLoan> getActiveLoan() {
        return getLoans().stream()
            .filter(loan -> loan.getStatus().isActive())
            .findFirst();
    }
}
