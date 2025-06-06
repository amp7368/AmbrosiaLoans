package com.ambrosia.loans.database.entity.client;

import com.ambrosia.loans.database.account.DClientInvestSnapshot;
import com.ambrosia.loans.database.account.DClientLoanSnapshot;
import com.ambrosia.loans.database.account.DClientSnapshot;
import com.ambrosia.loans.database.account.base.AccountEventType;
import com.ambrosia.loans.database.account.loan.DLoan;
import com.ambrosia.loans.database.account.loan.InterestCheckpoint;
import com.ambrosia.loans.database.entity.client.username.ClientDiscordDetails;
import com.ambrosia.loans.database.entity.client.username.ClientMinecraftDetails;
import com.ambrosia.loans.database.version.ApiVersionList.ApiVersionListLoan;
import com.ambrosia.loans.discord.DiscordBot;
import com.ambrosia.loans.discord.base.gui.ClientGui;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileInvestPage;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileLoanPage;
import com.ambrosia.loans.discord.command.player.profile.page.ProfileOverviewPage;
import com.ambrosia.loans.util.emerald.Emeralds;
import discord.util.dcf.gui.base.GuiReplyFirstMessage;
import io.ebean.DB;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.User;

public interface ClientAccess {

    private DClientSnapshot newLoanSnapshot(InterestCheckpoint checkpoint, Instant timestamp, long delta,
        AccountEventType eventType,
        Transaction transaction) {
        DClient client = getEntity();
        Emeralds newLoanBalance = client.addLoanBalance(delta, timestamp);
        DClientLoanSnapshot snapshot = new DClientLoanSnapshot(checkpoint, client, timestamp,
            newLoanBalance.amount(), delta, eventType);
        client.addAccountSnapshot(snapshot);
        snapshot.save(transaction);
        client.save(transaction);
        return snapshot;
    }

    private DClientSnapshot newInvestSnapshot(Instant timestamp, long delta, AccountEventType eventType, Transaction transaction) {
        DClient client = getEntity();
        Emeralds newLoanBalance = client.addInvestBalance(delta, timestamp);
        DClientInvestSnapshot snapshot = new DClientInvestSnapshot(client, timestamp,
            newLoanBalance.amount(), delta, eventType);
        client.addAccountSnapshot(snapshot);
        snapshot.save(transaction);
        client.save(transaction);
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

    default void updateBalance(DLoan associatedLoan, long delta, Instant timestamp, AccountEventType eventType) {
        try (Transaction transaction = DB.beginTransaction()) {
            updateBalance(associatedLoan, delta, timestamp, eventType, transaction);
            transaction.commit();
        }
    }

    default void updateBalance(DLoan associatedLoan, long delta, Instant timestamp, AccountEventType eventType,
        Transaction transaction) {
        DClient client = getEntity();
        client.refresh();

        if (associatedLoan != null) {
            InterestCheckpoint nextCheckpoint;
            if (!associatedLoan.getVersion().is(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY)) {
                associatedLoan.refresh();
                InterestCheckpoint prevCheckpoint = associatedLoan.getLastCheckpoint();
                nextCheckpoint = associatedLoan.getInterest(prevCheckpoint.copy(), timestamp);
                long interestAsNegative = -nextCheckpoint.addInterest();
                if (interestAsNegative != 0) {
                    newLoanSnapshot(prevCheckpoint, timestamp, interestAsNegative, AccountEventType.INTEREST, transaction);
                }
            } else {
                nextCheckpoint = new InterestCheckpoint(associatedLoan);
            }
            // todo idk why checkLoansPaid has to go before newLoanSnapshot, but it does
            checkLoansPaid(timestamp, transaction);
            newLoanSnapshot(nextCheckpoint, timestamp, delta, eventType, transaction);
        } else {
            newInvestSnapshot(timestamp, delta, eventType, transaction);
        }
    }

    private void checkLoansPaid(Instant timestamp, Transaction transaction) {
        // actually marks loans as paid
        List<DLoan> paidLoans = getEntity().getLoans().stream()
            .filter(DLoan::isActive)
            .filter(loan -> loan.getStartDate().isBefore(timestamp))
            .filter(loan -> loan.getEndDate() == null)
            .filter(loan -> loan.checkIsPaid(timestamp, transaction)).toList();

        paidLoans.stream()
            .filter(loan -> loan.getVersion().is(ApiVersionListLoan.SIMPLE_INTEREST_WEEKLY))
            .forEach(loan -> {
                InterestCheckpoint checkpoint = loan.getInterest(null, timestamp);
                long interestAsNegative = -checkpoint.addInterest();
                if (interestAsNegative != 0)
                    newLoanSnapshot(checkpoint, timestamp, interestAsNegative, AccountEventType.INTEREST, transaction);
            });
    }

    default ClientGui profile(GuiReplyFirstMessage createFirstMessage) {
        ClientGui gui = new ClientGui(this.getEntity(), DiscordBot.dcf, createFirstMessage);
        gui.addPage(new ProfileOverviewPage(gui))
            .addPage(new ProfileLoanPage(gui))
            .addPage(new ProfileInvestPage(gui));

        return gui;
    }

    default Optional<DLoan> getActiveLoan() {
        return getLoans().stream()
            .filter(loan -> loan.getStatus().isActive())
            .findFirst();
    }

    default List<DClientInvestSnapshot> getProfits() {
        return getEntity().getInvestSnapshots()
            .stream()
            .filter(snap -> snap.getEventType().isProfit())
            .sorted()
            .toList();
    }
}
